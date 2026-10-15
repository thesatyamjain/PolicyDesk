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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSystemBackground
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

    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Settings",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = IosSystemBackground
    ) { paddingValues ->
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
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("App Lock (Biometrics / PIN)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IosLabelPrimary)
                            Text("Require authentication on launch", fontSize = 12.sp, color = IosLabelSecondary)
                        }
                    }
                    Switch(
                        checked = state.isAppLockEnabled,
                        onCheckedChange = viewModel::toggleAppLock,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AppleSystemGreen
                        )
                    )
                }
            }

            // SECTION 2: BYOK AI SETTINGS
            LiquidGlassGroupedSection(title = "AI Extraction (BYOK)") {
                Text(
                    text = "Bring your own key for PDF document extraction. Keys are stored only on this device inside the Android Keystore and never sent externally.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = IosLabelSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Choice
                Text(
                    text = "AI PROVIDER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosLabelSecondary,
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
                                .background(if (isSelected) AppleSystemBlue else Color(0x14767680), pillShape)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AppleSystemBlue else Slate200,
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
                                color = if (isSelected) Color.White else IosLabelPrimary
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

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection & Save Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = viewModel::testAiConnection,
                        enabled = state.aiConfig.apiKey.isNotBlank() && !state.isTestingAi,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleSystemBlue)
                    ) {
                        if (state.isTestingAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (state.isTestingAi) "Testing..." else "Test Connection", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (state.aiConfig.apiKey.isNotBlank()) {
                        OutlinedButton(
                            onClick = viewModel::removeAiKey,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleSystemRed)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
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
            LiquidGlassGroupedSection(title = "Google Drive & Cloud Storage") {
                Text(
                    text = "All customer records and policies are stored securely on this device and synced directly to your personal Google Drive. No developer servers or third-party databases are used.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = IosLabelSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                DetailRow(label = "Total Records Stored", value = "${state.totalRecords} vehicles")
                DetailRow(label = "Architecture", value = "Local-First SQLite")
                DetailRow(label = "Cloud Provider", value = "Google Drive (SAF)")
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            backupSnapshotLauncher.launch("policydesk_backup.json")
                        },
                        enabled = !state.isSyncing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleSystemBlue)
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Backup to Drive", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            restoreSnapshotLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        enabled = !state.isSyncing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleSystemGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Drive", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleSystemBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val snapshot = viewModel.exportSnapshotJson()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_SUBJECT, "policydesk_snapshot.json")
                                    putExtra(Intent.EXTRA_TEXT, snapshot)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Database Snapshot"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IosLabelPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Backup", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
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
