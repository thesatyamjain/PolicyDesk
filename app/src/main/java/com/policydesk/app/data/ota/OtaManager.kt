package com.policydesk.app.data.ota

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class OtaReleaseInfo(
    val tagName: String,
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkSizeBytes: Long,
    val publishedAt: String
) {
    val displaySizeMb: String
        get() = String.format("%.1f MB", apkSizeBytes / (1024.0 * 1024.0))
}

sealed interface OtaState {
    object Idle : OtaState
    object Checking : OtaState
    object UpToDate : OtaState
    data class UpdateAvailable(val release: OtaReleaseInfo) : OtaState
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val release: OtaReleaseInfo
    ) : OtaState
    data class ReadyToInstall(val apkFile: File, val release: OtaReleaseInfo) : OtaState
    data class Error(val message: String) : OtaState
}

@Singleton
class OtaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val GITHUB_OWNER = "thesatyamjain"
        private const val GITHUB_REPO = "PolicyDesk"
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 30000
    }

    /**
     * Checks GitHub Releases API for a newer version than currentVersion.
     * Returns OtaReleaseInfo if a newer release exists with a valid APK, null if up-to-date.
     */
    suspend fun checkForUpdates(currentVersion: String): Result<OtaReleaseInfo?> =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(LATEST_RELEASE_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "PolicyDesk-Android")
                }

                val responseCode = conn.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorMsg = if (responseCode == 404) {
                        "No releases found on repository"
                    } else {
                        "GitHub API returned error code $responseCode"
                    }
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val json = JSONObject(jsonStr)
                val tagName = json.optString("tag_name", "")
                val releaseTitle = json.optString("name", tagName)
                val releaseNotes = json.optString("body", "")
                val publishedAt = json.optString("published_at", "")

                // Find attached APK asset
                var apkUrl = ""
                var apkSize = 0L
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        val contentType = asset.optString("content_type", "")
                        val downloadUrl = asset.optString("browser_download_url", "")
                        val size = asset.optLong("size", 0L)

                        if (name.endsWith(".apk", ignoreCase = true) ||
                            contentType == "application/vnd.android.package-archive"
                        ) {
                            apkUrl = downloadUrl
                            apkSize = size
                            break
                        }
                    }
                }

                if (apkUrl.isBlank()) {
                    return@withContext Result.failure(Exception("Release $tagName does not contain an APK asset"))
                }

                val latestCleanVersion = tagName.removePrefix("v").trim()
                val currentCleanVersion = currentVersion.removePrefix("v").trim()

                if (isNewerVersion(latestCleanVersion, currentCleanVersion)) {
                    val info = OtaReleaseInfo(
                        tagName = tagName,
                        versionName = latestCleanVersion,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        apkDownloadUrl = apkUrl,
                        apkSizeBytes = apkSize,
                        publishedAt = publishedAt
                    )
                    Result.success(info)
                } else {
                    Result.success(null) // Up to date
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Downloads the APK file to cache with progress tracking and follows redirects.
     */
    suspend fun downloadUpdate(
        release: OtaReleaseInfo,
        onProgress: (percent: Int, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) updatesDir.mkdirs()

            // Clean up any stale update APKs
            updatesDir.listFiles()?.forEach { it.delete() }

            val targetFile = File(updatesDir, "PolicyDesk-${release.versionName}.apk")

            var currentUrl = release.apkDownloadUrl
            var redirectCount = 0
            val maxRedirects = 6
            var connection: HttpURLConnection? = null

            // GitHub releases redirect to AWS S3 (objects.githubusercontent.com)
            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "PolicyDesk-Android")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == 307 || code == 308
                ) {
                    val nextUrl = connection.getHeaderField("Location") ?: ""
                    if (!nextUrl.startsWith("https://", ignoreCase = true)) {
                        connection.disconnect()
                        return@withContext Result.failure(SecurityException("Insecure redirect scheme rejected during OTA download: $nextUrl"))
                    }
                    currentUrl = nextUrl
                    connection.disconnect()
                    redirectCount++
                } else if (code == HttpURLConnection.HTTP_OK) {
                    break
                } else {
                    return@withContext Result.failure(Exception("HTTP error $code while downloading APK"))
                }
            }

            val finalConn = connection ?: return@withContext Result.failure(Exception("Failed to establish download connection"))
            val totalBytes = if (finalConn.contentLengthLong > 0) finalConn.contentLengthLong else release.apkSizeBytes

            finalConn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val percent = if (totalBytes > 0) {
                            ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else 0
                        onProgress(percent, totalRead, totalBytes)
                    }
                    output.flush()
                }
            }
            finalConn.disconnect()

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if install permission is granted and initiates Android Package Installer.
     */
    fun installUpdate(apkFile: File): Result<Unit> {
        return try {
            // Android 8.0+ Unknown App Sources Permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageIntent)
                    return Result.failure(Exception("Please allow PolicyDesk to install unknown apps, then tap Install again."))
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Semantic version compare: returns true if latest > current.
     * Handles versions like "0.1.1" vs "0.1.0", "1.0.0" vs "0.9.9".
     */
    fun isNewerVersion(latest: String, current: String): Boolean =
        OtaVersionComparator.isNewerVersion(latest, current)
}

object OtaVersionComparator {
    fun isNewerVersion(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.removePrefix("v").split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
            val currentParts = current.removePrefix("v").split('.').map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }

            val maxParts = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxParts) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        } catch (_: Exception) {
            return latest != current
        }
    }
}
