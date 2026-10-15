package com.policydesk.app.ui.settings

import android.content.Context
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

data class SettingsUiState(
    val aiConfig: AiConfig = AiConfig(),
    val isTestingAi: Boolean = false,
    val aiTestMessage: String? = null,
    val aiTestSuccess: Boolean = false,
    val googleAccountEmail: String? = null,
    val lastSyncTime: String = "Not synced yet",
    val isSyncing: Boolean = false,
    val syncStatusText: String = "Local-First Active",
    val syncMessage: String? = null,
    val totalRecords: Int = 0,
    val isAppLockEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VehicleRepository
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
        val lastSync = com.policydesk.app.data.drive.GoogleDriveManager.getLastSyncTime(context)
        val hasDriveUri = com.policydesk.app.data.drive.GoogleDriveManager.getSavedBackupUri(context) != null
        val syncTimeStr = if (lastSync > 0) {
            java.time.Instant.ofEpochMilli(lastSync)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
        } else "Not synced yet"

        viewModelScope.launch {
            val vehicles = repository.getAllVehicles().first()
            _uiState.update {
                it.copy(
                    aiConfig = config,
                    totalRecords = vehicles.size,
                    isAppLockEnabled = lockEnabled,
                    lastSyncTime = syncTimeStr,
                    syncStatusText = if (hasDriveUri) "Google Drive Auto-Sync Active" else "Local-First Active"
                )
            }
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
            it.copy(
                aiConfig = it.aiConfig.copy(apiKey = apiKey),
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
        val config = _uiState.value.aiConfig
        _uiState.update { it.copy(isTestingAi = true, aiTestMessage = null) }

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
            com.policydesk.app.data.drive.GoogleDriveManager.setSavedBackupUri(context, uri)

            val json = exportSnapshotJson()
            val result = com.policydesk.app.data.drive.GoogleDriveManager.writeSnapshotToUri(context, uri, json)
            val nowFormatted = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
            result.onSuccess {
                com.policydesk.app.data.drive.GoogleDriveManager.setLastSyncTime(context, System.currentTimeMillis())
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncTime = nowFormatted,
                        syncStatusText = "Google Drive Auto-Sync Active",
                        syncMessage = "Backup saved to your Google Drive! Daily auto-sync is now active."
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

    fun triggerSyncNow() {
        _uiState.update {
            val nowFormatted = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
            it.copy(
                lastSyncTime = nowFormatted,
                syncStatusText = "Local-First Active",
                syncMessage = "Use 'Backup to Google Drive' below to save your snapshot to Drive."
            )
        }
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
}
