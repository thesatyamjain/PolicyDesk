package com.policydesk.app.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.data.ai.AiExtractionClient
import com.policydesk.app.data.security.AiConfig
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.data.security.SecureKeyStorage
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

import com.policydesk.app.data.profile.AdvisorProfile
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.data.ota.OtaManager
import com.policydesk.app.data.ota.OtaReleaseInfo
import com.policydesk.app.data.ota.OtaState
import java.io.File
import com.policydesk.app.notification.NotificationHelper
import com.policydesk.app.notification.NotificationPreferenceManager
import androidx.core.app.NotificationManagerCompat
import android.os.Build

data class SettingsUiState(
    val aiConfig: AiConfig = AiConfig(),
    val isTestingAi: Boolean = false,
    val aiTestMessage: String? = null,
    val aiTestSuccess: Boolean = false,
    val googleAccountEmail: String? = null,
    val isDriveLinked: Boolean = false,
    val driveFileName: String = "policydesk_backup.json",
    val lastSyncTime: String = "Not synced yet",
    val isSyncing: Boolean = false,
    val syncStatusText: String = "Local-First Active",
    val syncMessage: String? = null,
    val totalRecords: Int = 0,
    val isAppLockEnabled: Boolean = false,
    val advisorProfile: AdvisorProfile = AdvisorProfile(),
    val areNotificationsEnabled: Boolean = true,
    val hasNotificationPermission: Boolean = true,
    val appVersionName: String = "1.0.0",
    val appVersionCode: Long = 1L,
    val otaState: OtaState = OtaState.Idle,
    val showOtaDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VehicleRepository,
    private val otaManager: OtaManager
) : ViewModel() {

    private val secureStorage = SecureKeyStorage(context)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        val config = secureStorage.getAiConfig()
        val lockEnabled = com.policydesk.app.data.security.AppLockManager.isAppLockEnabled(context)
        val isLinked = com.policydesk.app.data.drive.GoogleDriveManager.isDriveLinked(context)
        val fileName = com.policydesk.app.data.drive.GoogleDriveManager.SNAPSHOT_FILE_NAME
        val connectedAccount = com.policydesk.app.data.drive.GoogleDriveManager.getConnectedAccount(context)
        val lastSync = com.policydesk.app.data.drive.GoogleDriveManager.getLastSyncTime(context)
        val syncTimeStr = if (lastSync > 0) {
            java.time.Instant.ofEpochMilli(lastSync)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
        } else "Not synced yet"

        val profile = AdvisorProfileManager.getProfile(context)
        val notifEnabled = NotificationPreferenceManager.areNotificationsEnabled(context)
        val hasPerm = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val pkgInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) { null }
        val vName = pkgInfo?.versionName ?: "1.0.0"
        val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo?.longVersionCode ?: 1L
        } else {
            @Suppress("DEPRECATION")
            pkgInfo?.versionCode?.toLong() ?: 1L
        }

        viewModelScope.launch {
            val vehicles = repository.getAllVehicles().first()
            _uiState.update {
                it.copy(
                    aiConfig = config,
                    totalRecords = vehicles.size,
                    isAppLockEnabled = lockEnabled,
                    googleAccountEmail = connectedAccount,
                    isDriveLinked = isLinked,
                    driveFileName = fileName,
                    lastSyncTime = syncTimeStr,
                    syncStatusText = if (isLinked) "Google Drive Linked & Active" else "Local-First Active",
                    advisorProfile = profile,
                    areNotificationsEnabled = notifEnabled,
                    hasNotificationPermission = hasPerm,
                    appVersionName = vName,
                    appVersionCode = vCode
                )
            }
            checkForUpdates(manual = false)
        }
    }

    fun saveAdvisorProfile(profile: AdvisorProfile) {
        AdvisorProfileManager.saveProfile(context, profile)
        _uiState.update { it.copy(advisorProfile = profile) }
    }

    fun toggleNotifications(enabled: Boolean) {
        NotificationPreferenceManager.setNotificationsEnabled(context, enabled)
        _uiState.update { it.copy(areNotificationsEnabled = enabled) }
    }

    fun sendTestNotification() {
        NotificationHelper.showTestNotification(context)
    }

    fun refreshNotificationPermission() {
        val hasPerm = NotificationManagerCompat.from(context).areNotificationsEnabled()
        _uiState.update { it.copy(hasNotificationPermission = hasPerm) }
    }

    fun resetAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val vehicles = repository.getAllVehicles().first()
            if (vehicles.isNotEmpty()) {
                repository.softDeleteVehicles(vehicles.map { it.vehicle.regNo })
            }
            loadSettings()
            onComplete()
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        com.policydesk.app.data.security.AppLockManager.setAppLockEnabled(context, enabled)
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
    }

    fun onProviderSelected(provider: AiProvider) {
        _uiState.update {
            it.copy(
                aiConfig = it.aiConfig.copy(
                    provider = provider,
                    model = provider.defaultModel
                ),
                aiTestMessage = null
            )
        }
    }

    fun onApiKeyChanged(apiKey: String) {
        _uiState.update {
            val updatedConfig = if (it.aiConfig.provider == AiProvider.CLOUDFLARE_AI) {
                it.aiConfig.copy(customEndpoint = apiKey)
            } else {
                it.aiConfig.copy(apiKey = apiKey)
            }
            it.copy(
                aiConfig = updatedConfig,
                aiTestMessage = null
            )
        }
    }

    fun onCustomEndpointChanged(endpoint: String) {
        _uiState.update {
            it.copy(
                aiConfig = it.aiConfig.copy(customEndpoint = endpoint),
                aiTestMessage = null
            )
        }
    }

    fun onModelChanged(model: String) {
        _uiState.update {
            it.copy(
                aiConfig = it.aiConfig.copy(model = model),
                aiTestMessage = null
            )
        }
    }

    fun saveAiConfig() {
        val current = _uiState.value.aiConfig
        secureStorage.saveAiConfig(current)
    }

    fun removeAiKey() {
        secureStorage.removeAiKey()
        _uiState.update {
            it.copy(
                aiConfig = it.aiConfig.copy(apiKey = ""),
                aiTestMessage = null
            )
        }
    }

    fun testAiConnection() {
        val current = _uiState.value.aiConfig
        val cleanKey = current.apiKey.trim()
        var cleanModel = current.model.trim().removePrefix("models/")
        if (current.provider == AiProvider.GEMINI && (cleanModel.isBlank() || cleanModel == "gemini-1.5-flash")) {
            cleanModel = "gemini-2.5-flash"
        }
        val config = current.copy(apiKey = cleanKey, model = cleanModel)
        _uiState.update { it.copy(aiConfig = config, isTestingAi = true, aiTestMessage = null) }

        viewModelScope.launch {
            val result = AiExtractionClient.testConnection(config)
            result.onSuccess { msg ->
                saveAiConfig()
                _uiState.update {
                    it.copy(
                        isTestingAi = false,
                        aiTestMessage = msg,
                        aiTestSuccess = true
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isTestingAi = false,
                        aiTestMessage = err.message ?: "Connection test failed",
                        aiTestSuccess = false
                    )
                }
            }
        }
    }

    fun saveSnapshotToUri(uri: android.net.Uri) {
        _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
        viewModelScope.launch {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}

            var displayName = "policydesk_backup.json"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIdx)
                    }
                }
            } catch (_: Exception) {}

            com.policydesk.app.data.drive.GoogleDriveManager.setSavedBackupUri(context, uri, displayName)

            val json = exportSnapshotJson()
            val result = com.policydesk.app.data.drive.GoogleDriveManager.writeSnapshotToUri(context, uri, json)
            val nowFormatted = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
            result.onSuccess {
                com.policydesk.app.data.drive.GoogleDriveManager.setLastSyncTime(context, System.currentTimeMillis())
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        isDriveLinked = true,
                        driveFileName = displayName,
                        lastSyncTime = nowFormatted,
                        syncStatusText = "Google Drive Linked & Synced",
                        syncMessage = "Backup successfully linked to Google Drive ($displayName)!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncStatusText = "Save Failed",
                        syncMessage = err.message ?: "Failed to save backup to Google Drive"
                    )
                }
            }
        }
    }

    fun restoreSnapshotFromUri(uri: android.net.Uri) {
        _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
        viewModelScope.launch {
            val readResult = com.policydesk.app.data.drive.GoogleDriveManager.readSnapshotFromUri(context, uri)
            readResult.onSuccess { jsonStr ->
                val restoreResult = restoreSnapshotJson(jsonStr)
                restoreResult.onSuccess { count ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusText = "Restored from Drive",
                            syncMessage = "Successfully restored $count vehicle records from Google Drive backup!"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncMessage = "Corrupted backup: ${err.message}"
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = err.message ?: "Failed to read backup from Google Drive"
                    )
                }
            }
        }
    }

    fun getGoogleSignInIntent(): android.content.Intent {
        return com.policydesk.app.data.drive.GoogleDriveManager.getSignInClient(context).signInIntent
    }

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val email = account.email
            val displayName = account.displayName ?: email
            if (!email.isNullOrBlank()) {
                com.policydesk.app.data.drive.GoogleDriveManager.setConnectedAccount(context, email, displayName)
                _uiState.update {
                    it.copy(
                        googleAccountEmail = email,
                        isDriveLinked = true,
                        syncStatusText = "Google Drive Connected",
                        syncMessage = "Connected as $email. Syncing to 'PolicyDesk Vault'..."
                    )
                }
                triggerSyncNow()
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    syncStatusText = "Sign-In Failed",
                    syncMessage = "Google Sign-In was cancelled or failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun connectGoogleAccount(email: String) {
        com.policydesk.app.data.drive.GoogleDriveManager.setConnectedAccount(context, email)
        _uiState.update {
            it.copy(
                googleAccountEmail = email,
                isDriveLinked = true,
                syncStatusText = "Account Linked",
                syncMessage = "Account linked ($email). Tap 'Sync Now' to backup to PolicyDesk Vault."
            )
        }
        triggerSyncNow()
    }

    fun disconnectGoogleDrive() {
        com.policydesk.app.data.drive.GoogleDriveManager.disconnect(context)
        _uiState.update {
            it.copy(
                googleAccountEmail = null,
                isDriveLinked = false,
                driveFileName = com.policydesk.app.data.drive.GoogleDriveManager.SNAPSHOT_FILE_NAME,
                syncStatusText = "Local-First Active",
                syncMessage = "Google Drive disconnected. Data is stored safely on this device."
            )
        }
    }

    fun triggerSyncNow() {
        _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
        viewModelScope.launch {
            val snapshot = exportSnapshotJson()
            if (com.policydesk.app.data.drive.GoogleDriveManager.isDriveLinked(context)) {
                val driveResult = com.policydesk.app.data.drive.GoogleDriveManager.uploadToDriveVault(context, snapshot)
                val nowFormatted = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
                driveResult.onSuccess {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            isDriveLinked = true,
                            lastSyncTime = nowFormatted,
                            syncStatusText = "Synced with Drive Vault",
                            syncMessage = "All ${_uiState.value.totalRecords} records backed up to Google Drive (PolicyDesk Vault) successfully!"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncStatusText = "Sync Warning",
                            syncMessage = err.message ?: "Could not upload to Google Drive Vault"
                        )
                    }
                }
            } else {
                com.policydesk.app.data.drive.GoogleDriveManager.performAutoSync(context, snapshot)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncStatusText = "Local-First Active",
                        syncMessage = "Data saved to device cache. Sign in to Google Drive to enable cloud backup."
                    )
                }
            }
        }
    }

    fun restoreFromDriveVault() {
        _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
        viewModelScope.launch {
            val result = com.policydesk.app.data.drive.GoogleDriveManager.restoreFromDriveVault(context)
            result.onSuccess { jsonStr ->
                val restoreResult = restoreSnapshotJson(jsonStr)
                restoreResult.onSuccess { count ->
                    val nowFormatted = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastSyncTime = nowFormatted,
                            syncStatusText = "Restored from Drive",
                            syncMessage = "Successfully restored $count vehicle records from Google Drive Vault!"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncMessage = "Corrupted backup: ${err.message}"
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = err.message ?: "Failed to read backup from Google Drive Vault"
                    )
                }
            }
        }
    }

    suspend fun getShareableBackupUri(): android.net.Uri {
        val json = exportSnapshotJson()
        return com.policydesk.app.data.drive.GoogleDriveManager.createShareableBackupUri(context, json)
    }

    suspend fun exportDatabaseCsv(): String = withContext(Dispatchers.IO) {
        val vehicles = repository.getAllVehicles().first()
        val sb = StringBuilder()
        sb.append(com.policydesk.app.data.importer.SpreadsheetParser.TEMPLATE_CSV_HEADER.joinToString(",")).append("\n")

        for (v in vehicles) {
            val p = v.currentPolicy
            val row = listOf(
                v.vehicle.regNo,
                v.vehicle.customer.name,
                v.vehicle.customer.title ?: "",
                v.vehicle.customer.mobile,
                v.vehicle.customer.email ?: "",
                v.vehicle.customer.address,
                p?.companySector?.displayName ?: "",
                p?.insurer ?: "",
                p?.policyNumber ?: "",
                p?.expiryDate?.let { com.policydesk.app.ui.components.UiFormatters.formatDate(it) } ?: "",
                p?.totalPremium?.toString() ?: "",
                v.vehicle.makeModel ?: "",
                v.vehicle.vin ?: "",
                v.vehicle.engineNo ?: "",
                v.vehicle.notes ?: "",
                p?.saleDate?.let { com.policydesk.app.ui.components.UiFormatters.formatDate(it) } ?: ""
            )
            sb.append(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }).append("\n")
        }
        sb.toString()
    }

    suspend fun exportSnapshotJson(): String = withContext(Dispatchers.IO) {
        val vehicles = repository.getAllVehicles().first()
        com.policydesk.app.data.drive.GoogleDriveManager.createSnapshotJson(vehicles)
    }

    suspend fun restoreSnapshotJson(jsonStr: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val list = com.policydesk.app.data.drive.GoogleDriveManager.parseSnapshotJson(jsonStr)
            val count = repository.restoreVehiclesAndPolicies(list)
            val all = repository.getAllVehicles().first()
            _uiState.update { it.copy(totalRecords = all.size) }
            count
        }
    }

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(otaState = OtaState.Checking) }
            val currentVersion = uiState.value.appVersionName
            val result = otaManager.checkForUpdates(currentVersion)

            result.fold(
                onSuccess = { release ->
                    if (release != null) {
                        _uiState.update {
                            it.copy(
                                otaState = OtaState.UpdateAvailable(release),
                                showOtaDialog = manual
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                otaState = OtaState.UpToDate,
                                showOtaDialog = false
                            )
                        }
                        if (manual) {
                            Toast.makeText(context, "PolicyDesk is up to date (v$currentVersion)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(otaState = OtaState.Error(error.message ?: "Failed to check updates"))
                    }
                    if (manual) {
                        Toast.makeText(context, error.message ?: "Could not check updates", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    fun startOtaDownload() {
        val currentOta = uiState.value.otaState
        val release = when (currentOta) {
            is OtaState.UpdateAvailable -> currentOta.release
            is OtaState.Downloading -> currentOta.release
            is OtaState.ReadyToInstall -> currentOta.release
            else -> return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    otaState = OtaState.Downloading(
                        progressPercent = 0,
                        downloadedBytes = 0,
                        totalBytes = release.apkSizeBytes,
                        release = release
                    )
                )
            }

            val downloadResult = otaManager.downloadUpdate(release) { percent, downloaded, total ->
                _uiState.update {
                    it.copy(
                        otaState = OtaState.Downloading(
                            progressPercent = percent,
                            downloadedBytes = downloaded,
                            totalBytes = total,
                            release = release
                        )
                    )
                }
            }

            downloadResult.fold(
                onSuccess = { apkFile ->
                    _uiState.update {
                        it.copy(otaState = OtaState.ReadyToInstall(apkFile, release))
                    }
                    installOtaUpdate(apkFile)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(otaState = OtaState.Error(error.message ?: "Download failed"))
                    }
                    Toast.makeText(context, error.message ?: "Download failed", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun installOtaUpdate(file: File? = null) {
        val apkFile = file ?: (uiState.value.otaState as? OtaState.ReadyToInstall)?.apkFile ?: return
        val installResult = otaManager.installUpdate(apkFile)
        installResult.onFailure { error ->
            Toast.makeText(context, error.message ?: "Could not open installer", Toast.LENGTH_LONG).show()
        }
    }

    fun openOtaDialog() {
        _uiState.update { it.copy(showOtaDialog = true) }
    }

    fun dismissOtaDialog() {
        _uiState.update { it.copy(showOtaDialog = false) }
    }
}
