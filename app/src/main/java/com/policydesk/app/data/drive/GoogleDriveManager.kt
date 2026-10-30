package com.policydesk.app.data.drive

import android.content.Context
import android.net.Uri
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
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Clean Google Drive backup & restore service using personal Google Drive.
 * Uses 'drive.file' scope so app can only create & read files it created in 'PolicyDesk Vault'.
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
        // Check GoogleSignIn last account as fallback
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

    // Storage Access Framework legacy support
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
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Finds or creates the dedicated "PolicyDesk Vault" folder on the user's personal Google Drive.
     */
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
     * Uploads the policy snapshot directly into the "PolicyDesk Vault" folder on Google Drive.
     */
    suspend fun uploadToDriveVault(context: Context, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getAccessToken(context)
                ?: throw IllegalStateException("Google Drive is not linked. Please tap Sign In.")
            val folderId = getOrCreateVaultFolder(token).getOrThrow()

            // Check if policydesk_backup.json exists in the folder
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

            val fileId: String
            if (filesArray != null && filesArray.length() > 0) {
                fileId = filesArray.getJSONObject(0).getString("id")
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

            // Cache locally and save last sync timestamp
            val cacheFile = java.io.File(context.filesDir, "google_drive_sync_backup.json")
            cacheFile.writeText(snapshotJson, Charsets.UTF_8)
            setLastSyncTime(context, System.currentTimeMillis())
        }
    }

    /**
     * Downloads the policy backup from the "PolicyDesk Vault" folder on Google Drive.
     */
    suspend fun restoreFromDriveVault(context: Context): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getAccessToken(context)
                ?: throw IllegalStateException("Google Drive is not linked. Please tap Sign In.")
            val folderId = getOrCreateVaultFolder(token).getOrThrow()

            // Search for policydesk_backup.json in folder
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
            if (filesArray == null || filesArray.length() == 0) {
                throw IllegalStateException("No PolicyDesk backup file found in Google Drive Vault.")
            }

            val fileId = filesArray.getJSONObject(0).getString("id")

            // Download media
            val downloadUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            conn = (downloadUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 20000
                readTimeout = 20000
            }

            if (conn.responseCode !in 200..299) {
                val err = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("Failed to download backup: HTTP ${conn.responseCode} $err")
            }

            var content = conn.inputStream.bufferedReader().use { it.readText() }

            val root = runCatching { JSONObject(content) }.getOrNull()
            val vehiclesCount = root?.optJSONArray("vehicles")?.length() ?: 0

            // If the latest file is empty, check Drive file revision history for the last populated backup
            if (vehiclesCount == 0) {
                try {
                    val revUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId/revisions?fields=revisions(id,modifiedTime)")
                    val revConn = (revUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("Authorization", "Bearer $token")
                        connectTimeout = 15000
                        readTimeout = 15000
                    }
                    if (revConn.responseCode in 200..299) {
                        val revResp = revConn.inputStream.bufferedReader().use { it.readText() }
                        val revArray = JSONObject(revResp).optJSONArray("revisions")
                        if (revArray != null && revArray.length() > 0) {
                            for (r in (revArray.length() - 1) downTo 0) {
                                val revId = revArray.getJSONObject(r).getString("id")
                                val revDownloadUrl = URL("https://www.googleapis.com/drive/v3/files/$fileId/revisions/$revId?alt=media")
                                val rConn = (revDownloadUrl.openConnection() as HttpURLConnection).apply {
                                    requestMethod = "GET"
                                    setRequestProperty("Authorization", "Bearer $token")
                                    connectTimeout = 15000
                                    readTimeout = 15000
                                }
                                if (rConn.responseCode in 200..299) {
                                    val revContent = rConn.inputStream.bufferedReader().use { it.readText() }
                                    val revVehicles = runCatching { JSONObject(revContent).optJSONArray("vehicles")?.length() ?: 0 }.getOrDefault(0)
                                    if (revVehicles > 0) {
                                        content = revContent
                                        break
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            // Cache downloaded backup locally
            val cacheFile = java.io.File(context.filesDir, "google_drive_sync_backup.json")
            cacheFile.writeText(content, Charsets.UTF_8)
            setLastSyncTime(context, System.currentTimeMillis())

            content
        }
    }

    /**
     * Seamless auto-sync invoked on policy mutations and background workers.
     */
    suspend fun performAutoSync(context: Context, snapshotJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Safety guard: Never auto-sync empty records over an existing populated backup
            val root = runCatching { JSONObject(snapshotJson) }.getOrNull()
            val count = root?.optJSONArray("vehicles")?.length() ?: 0
            if (count == 0) {
                return@runCatching
            }

            // Internal app drive backup cache for instant offline restore / sync
            val cacheFile = java.io.File(context.filesDir, "google_drive_sync_backup.json")
            cacheFile.writeText(snapshotJson, Charsets.UTF_8)

            // If user has signed in to Google Drive, upload to PolicyDesk Vault
            if (isDriveLinked(context)) {
                uploadToDriveVault(context, snapshotJson).getOrThrow()
            }

            // Also update SAF uri if one was previously configured
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
        val backupDir = java.io.File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val backupFile = java.io.File(backupDir, SNAPSHOT_FILE_NAME)
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
            } ?: throw IllegalStateException("Unable to open output stream to Google Drive storage.")
        }
    }

    suspend fun readSnapshotFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
            } ?: throw IllegalStateException("Unable to open input stream from Google Drive storage.")
        }
    }

    fun createSnapshotJson(vehicles: List<VehicleWithPolicies>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("app", "PolicyDesk")

        val vehiclesArray = JSONArray()
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
        root.put("vehicles", vehiclesArray)
        return root.toString(2)
    }

    fun parseSnapshotJson(jsonStr: String): List<VehicleWithPolicies> {
        val root = JSONObject(jsonStr)
        val vehiclesArray = root.getJSONArray("vehicles")
        val list = mutableListOf<VehicleWithPolicies>()

        for (i in 0 until vehiclesArray.length()) {
            val vObj = vehiclesArray.getJSONObject(i)
            val cObj = vObj.getJSONObject("customer")
            val customer = Customer(
                title = cObj.optString("title").ifBlank { null },
                name = cObj.getString("name"),
                mobile = cObj.getString("mobile"),
                email = cObj.optString("email").ifBlank { null },
                address = cObj.getString("address")
            )
            val vehicle = Vehicle(
                regNo = vObj.getString("regNo"),
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

            val pArray = vObj.getJSONArray("policies")
            val policies = mutableListOf<Policy>()
            for (j in 0 until pArray.length()) {
                val pObj = pArray.getJSONObject(j)
                val policy = Policy(
                    id = pObj.getString("id"),
                    vehicleRegNo = pObj.getString("vehicleRegNo"),
                    companySector = Sector.valueOf(pObj.getString("companySector")),
                    insurer = pObj.getString("insurer"),
                    policyNumber = pObj.getString("policyNumber"),
                    expiryDate = pObj.getLong("expiryDate"),
                    startDate = pObj.optLong("startDate").takeIf { it > 0 },
                    totalPremium = pObj.getDouble("totalPremium"),
                    status = PolicyStatus.valueOf(pObj.getString("status")),
                    source = PolicySource.valueOf(pObj.getString("source")),
                    attachmentId = pObj.optString("attachmentId").ifBlank { null },
                    renewedOn = pObj.optLong("renewedOn").takeIf { it > 0 },
                    closeReason = pObj.optString("closeReason").ifBlank { null },
                    saleType = SaleType.valueOf(pObj.getString("saleType")),
                    saleDate = pObj.optLong("saleDate", System.currentTimeMillis()),
                    countsAsSale = pObj.optBoolean("countsAsSale", true)
                )
                policies.add(policy)
            }
            list.add(VehicleWithPolicies(vehicle, policies))
        }
        return list
    }
}
