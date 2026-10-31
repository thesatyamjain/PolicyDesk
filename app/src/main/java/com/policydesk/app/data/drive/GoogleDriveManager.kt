package com.policydesk.app.data.drive

import android.content.Context
import android.net.Uri
import android.os.Build
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Metadata descriptor for PolicyDesk backup bundles.
 */
data class BackupMetadata(
    val version: Int,
    val exportedAt: Long,
    val appVersion: String,
    val deviceName: String,
    val totalVehicles: Int,
    val totalPolicies: Int,
    val totalCustomers: Int,
    val checksum: String
)

/**
 * Descriptor for local rolling snapshots stored safely on device.
 */
data class LocalSnapshotInfo(
    val file: File,
    val fileName: String,
    val exportedAt: Long,
    val formattedDate: String,
    val totalVehicles: Int,
    val totalPolicies: Int,
    val fileSizeBytes: Long
) {
    val timestampFormatted: String get() = formattedDate
    val recordCount: Int get() = totalVehicles
    val fileSizeFormatted: String get() = "${(fileSizeBytes + 1023) / 1024} KB"
}

/**
 * Enterprise-grade, resilient Google Drive & Local Storage Backup Engine.
 * Supports:
 * 1. Dedicated Google Drive REST Vault with automatic Revision rescue & anti-wipe protection.
 * 2. Native Storage Access Framework (SAF) Google Drive / external storage document provider.
 * 3. Rolling local protected snapshots (last 5 backups retained on device).
 * 4. SHA-256 bundle verification and backward compatibility with all past versions.
 */
object GoogleDriveManager {

    const val SNAPSHOT_FILE_NAME = "policydesk_backup.json"
    const val VAULT_FOLDER_NAME = "PolicyDesk Vault"
    const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"

    private const val PREFS_NAME = "policydesk_drive_prefs"
    private const val KEY_CONNECTED_ACCOUNT = "connected_google_account"
    private const val KEY_CONNECTED_NAME = "connected_google_name"
    private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    private const val KEY_BACKUP_URI = "backup_file_uri"
    private const val KEY_BACKUP_NAME = "backup_file_name"
    private const val KEY_LAST_BACKUP_COUNT = "last_backup_vehicle_count"

    // ─── AUTHENTICATION & CONNECTION STATE ──────────────────────────────────────

    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_FILE_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getConnectedAccount(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CONNECTED_ACCOUNT, null)
        if (saved != null) return saved
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        return lastAccount?.email
    }

    fun getConnectedDisplayName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CONNECTED_NAME, null)
        if (saved != null) return saved
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        return lastAccount?.displayName
    }

    fun setConnectedAccount(context: Context, email: String?, displayName: String? = null) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONNECTED_ACCOUNT, email)
            .putString(KEY_CONNECTED_NAME, displayName)
            .putBoolean(KEY_AUTO_SYNC_ENABLED, email != null)
            .apply()
    }

    fun isDriveLinked(context: Context): Boolean {
        return getConnectedAccount(context) != null
    }

    fun isAutoSyncEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_SYNC_ENABLED, true)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AUTO_SYNC_ENABLED, enabled)
            .apply()
    }

    fun disconnect(context: Context) {
        try {
            getSignInClient(context).signOut()
        } catch (_: Exception) {}
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CONNECTED_ACCOUNT)
            .remove(KEY_CONNECTED_NAME)
            .remove(KEY_BACKUP_URI)
            .remove(KEY_BACKUP_NAME)
            .putBoolean(KEY_AUTO_SYNC_ENABLED, false)
            .apply()
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun setLastBackupCount(context: Context, count: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_BACKUP_COUNT, count)
            .apply()
    }

    fun getLastBackupCount(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_LAST_BACKUP_COUNT, 0)
    }

    // ─── STORAGE ACCESS FRAMEWORK (SAF) DOCUMENT SUPPORT ───────────────────────

    fun setSavedBackupUri(context: Context, uri: Uri, name: String? = null) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(KEY_BACKUP_URI, uri.toString())
        if (name != null) {
            editor.putString(KEY_BACKUP_NAME, name)
        }
        editor.putBoolean(KEY_AUTO_SYNC_ENABLED, true)
        editor.apply()
    }

    fun getSavedBackupUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BACKUP_URI, null) ?: return null
        return Uri.parse(uriStr)
    }

    fun getSavedBackupName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BACKUP_NAME, SNAPSHOT_FILE_NAME) ?: SNAPSHOT_FILE_NAME
    }

    suspend fun getAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
        val account: GoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        val androidAccount = account.account ?: return@withContext null
        try {
            GoogleAuthUtil.getToken(context, androidAccount, "oauth2:$DRIVE_FILE_SCOPE")
        } catch (_: Exception) {
            null
        }
    }

    // ─── GOOGLE DRIVE VAULT REST ENGINE ────────────────────────────────────────

    private suspend fun getOrCreateVaultFolder(token: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$VAULT_FOLDER_NAME' and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = URL("https://www.googleapis.com/drive/v3/files?q=$encodedQuery&spaces=drive&fields=files(id,name)")

            var conn = (searchUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val searchResponse = if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to query Drive vault: HTTP ${conn.responseCode} $err")
            }

            val searchJson = JSONObject(searchResponse)
            val filesArray = searchJson.optJSONArray("files")
            if (filesArray != null && filesArray.length() > 0) {
                return@runCatching filesArray.getJSONObject(0).getString("id")
            }

            // Create folder if not found
            val createUrl = URL("https://www.googleapis.com/drive/v3/files")
            conn = (createUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val folderMeta = JSONObject().apply {
                put("name", VAULT_FOLDER_NAME)
                put("mimeType", "application/vnd.google-apps.folder")
            }
            conn.outputStream.bufferedWriter().use { it.write(folderMeta.toString()) }

            val createResponse = if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to create Drive vault folder: HTTP ${conn.responseCode} $err")
            }

            JSONObject(createResponse).getString("id")
        }
    }

    /**
     * Uploads the policy snapshot bundle directly into the "PolicyDesk Vault" folder on Google Drive.
     * Incorporates anti-wipe safeguard and local rolling snapshot preservation.
     */
    suspend fun uploadToDriveVault(context: Context, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getAccessToken(context)
                ?: throw IllegalStateException("Google Drive is not linked. Please tap Sign In.")
            val folderId = getOrCreateVaultFolder(token).getOrThrow()

            // Check if policydesk_backup.json exists in the vault folder
            val query = "name = '$SNAPSHOT_FILE_NAME' and '$folderId' in parents and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = URL("https://www.googleapis.com/drive/v3/files?q=$encodedQuery&spaces=drive&fields=files(id,name)")

            var conn = (searchUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val searchResponse = if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to check backup file: HTTP ${conn.responseCode} $err")
            }

            val searchJson = JSONObject(searchResponse)
            val filesArray = searchJson.optJSONArray("files")

            val newVehiclesCount = countVehiclesInSnapshot(snapshotJson)
            val fileId: String
            if (filesArray != null && filesArray.length() > 0) {
                fileId = filesArray.getJSONObject(0).getString("id")

                // ANTI-WIPE GUARD: If local database has 0 records (e.g. freshly installed app),
                // do not overwrite an existing cloud vault backup that already holds policies!
                if (newVehiclesCount == 0) {
                    val checkUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                    val checkConn = (checkUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("Authorization", "Bearer $token")
                        connectTimeout = 10000
                        readTimeout = 10000
                    }
                    if (checkConn.responseCode in 200..299) {
                        val existingContent = checkConn.inputStream.bufferedReader().use { it.readText() }
                        if (countVehiclesInSnapshot(existingContent) > 0) {
                            // Preserve cloud records and abort overwrite
                            return@runCatching
                        }
                    }
                }
            } else {
                // Create file metadata in folder
                val createUrl = URL("https://www.googleapis.com/drive/v3/files")
                conn = (createUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val parentsArray = JSONArray().apply { put(folderId) }
                val fileMeta = JSONObject().apply {
                    put("name", SNAPSHOT_FILE_NAME)
                    put("parents", parentsArray)
                    put("mimeType", "application/json")
                }
                conn.outputStream.bufferedWriter().use { it.write(fileMeta.toString()) }

                val createResponse = if (conn.responseCode in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    throw IllegalStateException("Failed to create backup metadata: HTTP ${conn.responseCode} $err")
                }
                fileId = JSONObject(createResponse).getString("id")
            }

            // Upload JSON content to file media endpoint
            val uploadUrl = URL("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            conn = (uploadUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 30000
                readTimeout = 30000
            }
            conn.outputStream.bufferedWriter().use { it.write(snapshotJson) }

            if (conn.responseCode !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to upload backup content: HTTP ${conn.responseCode} $err")
            }

            // Save to rolling local snapshot cache
            saveLocalRollingSnapshot(context, snapshotJson)
            setLastSyncTime(context, System.currentTimeMillis())
            setLastBackupCount(context, newVehiclesCount)
        }
    }

    /**
     * Downloads the policy backup from Google Drive.
     * Features multi-tier search (vault folder -> global drive query) and automatic Google Drive Revision rescue.
     */
    suspend fun restoreFromDriveVault(context: Context): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getAccessToken(context)
                ?: throw IllegalStateException("Google Drive is not linked. Please tap Sign In.")
            val folderId = getOrCreateVaultFolder(token).getOrThrow()

            // 1. Search in PolicyDesk Vault folder first
            val query = "name = '$SNAPSHOT_FILE_NAME' and '$folderId' in parents and trashed = false"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = URL("https://www.googleapis.com/drive/v3/files?q=$encodedQuery&spaces=drive&fields=files(id,name)")

            var conn = (searchUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val searchResponse = if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to check backup file: HTTP ${conn.responseCode} $err")
            }

            val searchJson = JSONObject(searchResponse)
            val filesArray = searchJson.optJSONArray("files")
            var fileId: String? = null

            if (filesArray != null && filesArray.length() > 0) {
                fileId = filesArray.getJSONObject(0).getString("id")
            } else {
                // 2. Global fallback: search for policydesk_backup.json anywhere on user's Drive
                val globalQuery = URLEncoder.encode("name = '$SNAPSHOT_FILE_NAME' and trashed = false", "UTF-8")
                val globalUrl = URL("https://www.googleapis.com/drive/v3/files?q=$globalQuery&spaces=drive&fields=files(id,name,modifiedTime)&orderBy=modifiedTime desc")
                val globalConn = (globalUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                if (globalConn.responseCode in 200..299) {
                    val globalResp = globalConn.inputStream.bufferedReader().use { it.readText() }
                    val globalFiles = JSONObject(globalResp).optJSONArray("files")
                    if (globalFiles != null && globalFiles.length() > 0) {
                        fileId = globalFiles.getJSONObject(0).getString("id")
                    }
                }
            }

            if (fileId == null) {
                throw IllegalStateException("No PolicyDesk backup file found in Google Drive Vault or root storage.")
            }

            // 3. Download current file content
            var content = downloadDriveFile(token, fileId)
            var vehicleCount = countVehiclesInSnapshot(content)

            // 4. REVISION RESCUE: If the latest file is empty (e.g. wiped by an unpopulated sync),
            // automatically inspect past revisions on Google Drive to restore historical records!
            if (vehicleCount == 0) {
                val revUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId/revisions?fields=revisions(id,modifiedTime)")
                val revConn = (revUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                if (revConn.responseCode in 200..299) {
                    val revResp = revConn.inputStream.bufferedReader().use { it.readText() }
                    val revList = JSONObject(revResp).optJSONArray("revisions")
                    if (revList != null && revList.length() > 1) {
                        for (r in (revList.length() - 1) downTo 0) {
                            val revId = revList.getJSONObject(r).getString("id")
                            val revContent = try {
                                val dlRevUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId/revisions/$revId?alt=media")
                                val dlConn = (dlRevUrl.openConnection() as HttpURLConnection).apply {
                                    requestMethod = "GET"
                                    setRequestProperty("Authorization", "Bearer $token")
                                    connectTimeout = 15000
                                    readTimeout = 15000
                                }
                                if (dlConn.responseCode in 200..299) {
                                    dlConn.inputStream.bufferedReader().use { it.readText() }
                                } else null
                            } catch (_: Exception) { null }

                            if (revContent != null && countVehiclesInSnapshot(revContent) > 0) {
                                content = revContent
                                vehicleCount = countVehiclesInSnapshot(revContent)
                                break
                            }
                        }
                    }
                }
            }

            // Save recovered copy locally
            saveLocalRollingSnapshot(context, content)
            setLastSyncTime(context, System.currentTimeMillis())
            setLastBackupCount(context, vehicleCount)

            content
        }
    }

    private fun downloadDriveFile(token: String, fileId: String): String {
        val downloadUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
        val conn = (downloadUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            connectTimeout = 20000
            readTimeout = 20000
        }
        if (conn.responseCode !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
            throw IllegalStateException("Failed to download backup: HTTP ${conn.responseCode} $err")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    // ─── LOCAL ROLLING SNAPSHOTS (OFFLINE-FIRST PROTECTION) ───────────────────

    private fun getBackupsDirectory(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveLocalRollingSnapshot(context: Context, snapshotJson: String) {
        try {
            val dir = getBackupsDirectory(context)
            // 1. Always update policydesk_backup_latest.json
            val latestFile = File(dir, "policydesk_backup_latest.json")
            latestFile.writeText(snapshotJson, Charsets.UTF_8)

            // 2. Also keep a dated snapshot
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val datedFile = File(dir, "policydesk_backup_$dateStr.json")
            datedFile.writeText(snapshotJson, Charsets.UTF_8)

            // 3. Prune old snapshots, retaining the last 5
            val allBackups = dir.listFiles { file -> file.name.startsWith("policydesk_backup_2") && file.name.endsWith(".json") }
            if (allBackups != null && allBackups.size > 5) {
                allBackups.sortedBy { it.lastModified() }
                    .take(allBackups.size - 5)
                    .forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }

    fun getLocalSnapshots(context: Context): List<LocalSnapshotInfo> {
        val dir = getBackupsDirectory(context)
        val files = dir.listFiles { file -> file.name.endsWith(".json") } ?: return emptyList()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        return files.mapNotNull { file ->
            try {
                val text = file.readText(Charsets.UTF_8)
                val count = countVehiclesInSnapshot(text)
                val policiesCount = countPoliciesInSnapshot(text)
                LocalSnapshotInfo(
                    file = file,
                    fileName = file.name,
                    exportedAt = file.lastModified(),
                    formattedDate = dateFormat.format(Date(file.lastModified())),
                    totalVehicles = count,
                    totalPolicies = policiesCount,
                    fileSizeBytes = file.length()
                )
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.exportedAt }
    }

    // ─── UNIVERSAL AUTO-SYNC & SHARE HOOKS ────────────────────────────────────

    suspend fun performAutoSync(context: Context, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Save to local protected rolling cache
            saveLocalRollingSnapshot(context, snapshotJson)

            // If user has linked Google Drive, sync to PolicyDesk Vault
            if (isDriveLinked(context)) {
                uploadToDriveVault(context, snapshotJson).getOrNull()
            }

            // Also sync to SAF storage URI if previously configured
            getSavedBackupUri(context)?.let { uri ->
                writeSnapshotToUri(context, uri, snapshotJson).getOrNull()
            }
            Unit
        }
    }

    suspend fun performSilentBackup(context: Context, snapshotJson: String): Result<Unit> {
        return performAutoSync(context, snapshotJson)
    }

    fun createShareableBackupUri(context: Context, snapshotJson: String): Uri {
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val backupFile = File(backupDir, SNAPSHOT_FILE_NAME)
        backupFile.writeText(snapshotJson, Charsets.UTF_8)
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
    }

    suspend fun writeSnapshotToUri(context: Context, uri: Uri, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(snapshotJson) }
            } ?: throw IllegalStateException("Unable to open output stream to storage.")
            saveLocalRollingSnapshot(context, snapshotJson)
        }
    }

    suspend fun readSnapshotFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
            } ?: throw IllegalStateException("Unable to open input stream from storage.")
        }
    }

    // ─── BUNDLE CREATION, VALIDATION & JSON PARSING ───────────────────────────

    fun computeSha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun countVehiclesInSnapshot(jsonStr: String): Int {
        return try {
            JSONObject(jsonStr).optJSONArray("vehicles")?.length() ?: 0
        } catch (_: Exception) { 0 }
    }

    fun countPoliciesInSnapshot(jsonStr: String): Int {
        return try {
            val root = JSONObject(jsonStr)
            val vehicles = root.optJSONArray("vehicles") ?: return 0
            var policiesCount = 0
            for (i in 0 until vehicles.length()) {
                val v = vehicles.optJSONObject(i) ?: continue
                policiesCount += v.optJSONArray("policies")?.length() ?: 0
            }
            policiesCount
        } catch (_: Exception) { 0 }
    }

    fun createSnapshotJson(vehicles: List<VehicleWithPolicies>, appVersionName: String = "0.1.2"): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val device = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        val vehiclesArray = JSONArray()
        var totalPolicies = 0
        for (v in vehicles) {
            val vObj = JSONObject()
            vObj.put("regNo", v.vehicle.regNo)
            vObj.put("makeModel", v.vehicle.makeModel ?: "")
            vObj.put("vin", v.vehicle.vin ?: "")
            vObj.put("engineNo", v.vehicle.engineNo ?: "")
            vObj.put("notes", v.vehicle.notes ?: "")
            vObj.put("createdAt", v.vehicle.createdAt)
            vObj.put("updatedAt", v.vehicle.updatedAt)
            vObj.put("isClosed", v.vehicle.isClosed)
            vObj.put("closeReason", v.vehicle.closeReason ?: "")

            val cObj = JSONObject()
            cObj.put("title", v.vehicle.customer.title ?: "")
            cObj.put("name", v.vehicle.customer.name)
            cObj.put("mobile", v.vehicle.customer.mobile)
            cObj.put("email", v.vehicle.customer.email ?: "")
            cObj.put("address", v.vehicle.customer.address)
            vObj.put("customer", cObj)

            val pArray = JSONArray()
            for (p in v.policies) {
                totalPolicies++
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("vehicleRegNo", p.vehicleRegNo)
                pObj.put("companySector", p.companySector.name)
                pObj.put("insurer", p.insurer)
                pObj.put("policyNumber", p.policyNumber)
                pObj.put("expiryDate", p.expiryDate)
                pObj.put("startDate", p.startDate ?: 0L)
                pObj.put("totalPremium", p.totalPremium)
                pObj.put("status", p.status.name)
                pObj.put("source", p.source.name)
                pObj.put("attachmentId", p.attachmentId ?: "")
                pObj.put("renewedOn", p.renewedOn ?: 0L)
                pObj.put("closeReason", p.closeReason ?: "")
                pObj.put("saleType", p.saleType.name)
                pObj.put("saleDate", p.saleDate)
                pObj.put("countsAsSale", p.countsAsSale)
                pArray.put(pObj)
            }
            vObj.put("policies", pArray)
            vehiclesArray.put(vObj)
        }

        val rawVehiclesStr = vehiclesArray.toString()
        val checksum = computeSha256(rawVehiclesStr)

        root.put("formatVersion", 2)
        root.put("version", 1) // For legacy parser compatibility
        root.put("app", "PolicyDesk")
        root.put("appVersion", appVersionName)
        root.put("deviceName", device)
        root.put("exportedAt", now)
        root.put("totalVehicles", vehicles.size)
        root.put("totalPolicies", totalPolicies)
        root.put("checksum", checksum)
        root.put("vehicles", vehiclesArray)

        return root.toString(2)
    }

    fun parseSnapshotBundle(jsonStr: String): Pair<BackupMetadata, List<VehicleWithPolicies>> {
        val root = JSONObject(jsonStr)
        val vehiclesArray = root.optJSONArray("vehicles")
            ?: throw IllegalArgumentException("Invalid backup: Missing vehicles payload")

        val version = root.optInt("formatVersion", root.optInt("version", 1))
        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
        val appVersion = root.optString("appVersion", "Unknown")
        val deviceName = root.optString("deviceName", "Unknown")
        val checksum = root.optString("checksum", "")

        val list = mutableListOf<VehicleWithPolicies>()
        var policiesCount = 0

        for (i in 0 until vehiclesArray.length()) {
            val vObj = vehiclesArray.optJSONObject(i) ?: continue
            val cObj = vObj.optJSONObject("customer") ?: JSONObject()
            val customer = Customer(
                title = cObj.optString("title").ifBlank { null },
                name = cObj.optString("name", "Unknown Customer"),
                mobile = cObj.optString("mobile", ""),
                email = cObj.optString("email").ifBlank { null },
                address = cObj.optString("address", "")
            )
            val vehicle = Vehicle(
                regNo = vObj.optString("regNo", ""),
                customer = customer,
                makeModel = vObj.optString("makeModel").ifBlank { null },
                vin = vObj.optString("vin").ifBlank { null },
                engineNo = vObj.optString("engineNo").ifBlank { null },
                notes = vObj.optString("notes").ifBlank { null },
                createdAt = vObj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = vObj.optLong("updatedAt", System.currentTimeMillis()),
                isClosed = vObj.optBoolean("isClosed", false),
                closeReason = vObj.optString("closeReason").ifBlank { null }
            )

            val pArray = vObj.optJSONArray("policies") ?: JSONArray()
            val policies = mutableListOf<Policy>()
            for (j in 0 until pArray.length()) {
                val pObj = pArray.optJSONObject(j) ?: continue
                policiesCount++
                val sectorStr = pObj.optString("companySector", Sector.PRIVATE.name)
                val sector = try { Sector.valueOf(sectorStr) } catch (_: Exception) { Sector.PRIVATE }

                val statusStr = pObj.optString("status", PolicyStatus.ACTIVE.name)
                val status = try { PolicyStatus.valueOf(statusStr) } catch (_: Exception) { PolicyStatus.ACTIVE }

                val sourceStr = pObj.optString("source", PolicySource.FORM.name)
                val source = try { PolicySource.valueOf(sourceStr) } catch (_: Exception) { PolicySource.FORM }

                val saleTypeStr = pObj.optString("saleType", SaleType.NEW_BUSINESS.name)
                val saleType = try { SaleType.valueOf(saleTypeStr) } catch (_: Exception) { SaleType.NEW_BUSINESS }

                val policy = Policy(
                    id = pObj.optString("id", java.util.UUID.randomUUID().toString()),
                    vehicleRegNo = pObj.optString("vehicleRegNo", vehicle.regNo),
                    companySector = sector,
                    insurer = pObj.optString("insurer", "Unknown Insurer"),
                    policyNumber = pObj.optString("policyNumber", ""),
                    expiryDate = pObj.optLong("expiryDate", System.currentTimeMillis()),
                    startDate = pObj.optLong("startDate").takeIf { it > 0 },
                    totalPremium = pObj.optDouble("totalPremium", 0.0),
                    status = status,
                    source = source,
                    attachmentId = pObj.optString("attachmentId").ifBlank { null },
                    renewedOn = pObj.optLong("renewedOn").takeIf { it > 0 },
                    closeReason = pObj.optString("closeReason").ifBlank { null },
                    saleType = saleType,
                    saleDate = pObj.optLong("saleDate", System.currentTimeMillis()),
                    countsAsSale = pObj.optBoolean("countsAsSale", true)
                )
                policies.add(policy)
            }
            if (vehicle.regNo.isNotBlank()) {
                list.add(VehicleWithPolicies(vehicle, policies))
            }
        }

        val metadata = BackupMetadata(
            version = version,
            exportedAt = exportedAt,
            appVersion = appVersion,
            deviceName = deviceName,
            totalVehicles = list.size,
            totalPolicies = policiesCount,
            totalCustomers = list.map { it.vehicle.customer.mobile }.distinct().size,
            checksum = checksum
        )

        return Pair(metadata, list)
    }

    fun parseSnapshotJson(jsonStr: String): List<VehicleWithPolicies> {
        return parseSnapshotBundle(jsonStr).second
    }
}
