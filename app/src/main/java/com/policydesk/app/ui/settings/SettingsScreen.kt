package com.policydesk.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.AppleSwitch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.LiquidGlassGroupedSection
import com.policydesk.app.ui.components.LiquidGlassListRow
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkTertiaryGrouped
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSystemBackground
import com.policydesk.app.ui.theme.IosTertiaryGrouped
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate400
import com.policydesk.app.ui.theme.Slate50
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val backupSnapshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.saveSnapshotToUri(uri)
        }
    }

    val restoreSnapshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreSnapshotFromUri(uri)
        }
    }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (!email.isNullOrBlank()) {
                viewModel.connectGoogleAccount(email)
            }
        }
    }

    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Settings",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: SECURITY & APP LOCK
            LiquidGlassGroupedSection(title = "Security & Privacy") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(AppleSystemBlue, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.ShieldCheck,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("App Lock (Biometrics / PIN)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = labelPrimary)
                            Text("Require authentication on launch", fontSize = 12.sp, color = labelSecondary)
                        }
                    }
                    AppleSwitch(
                        checked = state.isAppLockEnabled,
                        onCheckedChange = viewModel::toggleAppLock
                    )
                }
            }

            // SECTION 2: BYOK AI SETTINGS
            LiquidGlassGroupedSection(title = "AI Extraction (BYOK)") {
                Text(
                    text = "Bring your own key for PDF document extraction. Keys are stored only on this device inside the Android Keystore and never sent externally.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = labelSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Choice
                Text(
                    text = "AI PROVIDER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelSecondary,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProvider.values().forEach { provider ->
                        val isSelected = state.aiConfig.provider == provider
                        val pillShape = RoundedCornerShape(10.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .background(if (isSelected) AppleSystemBlue else (if (isDark) Color(0x33545458) else Color(0x14767680)), pillShape)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AppleSystemBlue else (if (isDark) Color(0x33FFFFFF) else Slate200),
                                    shape = pillShape
                                )
                                .clickable { viewModel.onProviderSelected(provider) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = provider.displayName.replace("Google ", "").replace("Anthropic ", ""),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else labelPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // API Key Field
                OutlinedTextField(
                    value = state.aiConfig.apiKey,
                    onValueChange = viewModel::onApiKeyChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") },
                    placeholder = { Text("Paste your API key here") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Key",
                                tint = Slate400
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Model Field
                OutlinedTextField(
                    value = state.aiConfig.model,
                    onValueChange = viewModel::onModelChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Model Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                // Recommended Model Quick Selector Pills
                val suggestedModels = when (state.aiConfig.provider) {
                    AiProvider.GEMINI -> listOf("gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.0-flash")
                    AiProvider.OPENAI -> listOf("gpt-4o-mini", "gpt-4o")
                    AiProvider.CLAUDE -> listOf("claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedModels.forEach { modelOption ->
                        val isSelected = state.aiConfig.model.equals(modelOption, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) AppleSystemBlue.copy(alpha = 0.15f)
                                    else if (isDark) IosDarkTertiaryGrouped
                                    else IosTertiaryGrouped
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AppleSystemBlue else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onModelChanged(modelOption) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = modelOption,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AppleSystemBlue else if (isDark) IosDarkLabelSecondary else IosLabelSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection & Save Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppleButton(
                        text = if (state.isTestingAi) "Testing..." else "Test Connection",
                        onClick = viewModel::testAiConnection,
                        enabled = state.aiConfig.apiKey.isNotBlank() && !state.isTestingAi,
                        isLoading = state.isTestingAi,
                        modifier = Modifier.weight(1.3f)
                    )

                    if (state.aiConfig.apiKey.isNotBlank()) {
                        AppleSecondaryButton(
                            text = "Remove",
                            onClick = viewModel::removeAiKey,
                            tintColor = AppleSystemRed,
                            icon = Icons.Default.Delete,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Test Connection Feedback Banner
                state.aiTestMessage?.let { msg ->
                    val isSuccess = state.aiTestSuccess
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSuccess) AppleSystemGreen.copy(alpha = 0.12f) else AppleSystemRed.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSuccess) AppleSystemGreen.copy(alpha = 0.35f) else AppleSystemRed.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (isSuccess) AppleSystemGreen else AppleSystemRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // SECTION 3: GOOGLE DRIVE & CLOUD STORAGE
            LiquidGlassGroupedSection(title = "Google Drive Sync") {
                if (state.googleAccountEmail != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppleSystemGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .border(1.dp, AppleSystemGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Drive Connected",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleSystemGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.googleAccountEmail ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Auto-Sync Active (Every policy & renewal)",
                                fontSize = 11.sp,
                                color = AppleSystemGreen
                            )
                        }
                        androidx.compose.material3.TextButton(onClick = viewModel::disconnectGoogleDrive) {
                            Text("Disconnect", color = AppleSystemRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Total Records", value = "${state.totalRecords} vehicles")
                    DetailRow(label = "Sync Mode", value = "Automatic Background Sync")
                    DetailRow(label = "Last Synced Time", value = state.lastSyncTime)

                    state.syncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppleSystemBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = msg, fontSize = 12.sp, color = AppleSystemBlue, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleButton(
                            text = if (state.isSyncing) "Syncing..." else "Sync Now",
                            onClick = viewModel::triggerSyncNow,
                            enabled = !state.isSyncing,
                            isLoading = state.isSyncing,
                            modifier = Modifier.weight(1f)
                        )

                        AppleSecondaryButton(
                            text = "Export Backup",
                            onClick = { backupSnapshotLauncher.launch("policydesk_backup.json") },
                            enabled = !state.isSyncing,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        text = "Connect your Google Account to automatically sync all your customer policies, vehicles, and renewals to your personal Google Drive in the background.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = labelSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Storage", value = "Local-First + Google Drive")
                    DetailRow(label = "Status", value = "Not Connected")

                    state.syncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppleSystemBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = msg, fontSize = 12.sp, color = AppleSystemBlue, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppleButton(
                        text = "Connect Google Drive",
                        onClick = {
                            try {
                                accountPickerLauncher.launch(
                                    com.policydesk.app.data.drive.GoogleDriveManager.getGoogleAccountChooserIntent()
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open Google account picker: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppleSecondaryButton(
                        text = "Export CSV",
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportDatabaseCsv()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "PolicyDesk_Full_Export.csv")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export Records to CSV"))
                            }
                        },
                        tintColor = AppleSystemBlue,
                        modifier = Modifier.weight(1f)
                    )

                    AppleSecondaryButton(
                        text = "Restore File",
                        onClick = { restoreSnapshotLauncher.launch(arrayOf("application/json", "*/*")) },
                        tintColor = labelPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SECTION 4: NOTIFICATIONS & REMINDERS
            LiquidGlassGroupedSection(title = "Reminders & Alerts") {
                DetailRow(label = "Expiry Cadence", value = "30d, 15d, 7d, 1d & on day")
                DetailRow(label = "Daily Digest", value = "9:00 AM daily")
                DetailRow(label = "Reboot Recovery", value = "Auto-reschedules on boot")
                DetailRow(label = "Engine", value = "Local WorkManager & NotificationManager")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
