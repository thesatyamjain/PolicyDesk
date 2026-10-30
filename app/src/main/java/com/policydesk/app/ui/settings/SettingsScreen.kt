package com.policydesk.app.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.policydesk.app.data.profile.AdvisorProfile
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.data.ota.OtaState
import com.policydesk.app.ui.components.AppleAlertDialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.runtime.collectAsState
import com.policydesk.app.data.theme.AppTheme
import com.policydesk.app.data.theme.ThemeManager
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    var showKey by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    var advisorName by remember(state.advisorProfile.advisorName) { mutableStateOf(state.advisorProfile.advisorName) }
    var agencyName by remember(state.advisorProfile.agencyName) { mutableStateOf(state.advisorProfile.agencyName) }
    var advisorPhone by remember(state.advisorProfile.advisorPhone) { mutableStateOf(state.advisorProfile.advisorPhone) }
    var customTemplate by remember(state.advisorProfile.customWhatsappTemplate) { mutableStateOf(state.advisorProfile.customWhatsappTemplate) }
    var profileSavedFeedback by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshNotificationPermission()
    }

    if (showPrivacyDialog) {
        AppleAlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = "Privacy Policy",
            message = "PolicyDesk is strictly a Local-First application.\n\n" +
                    "• Zero Cloud Tracking: All your vehicle, policy, and client data is stored locally on this device in an encrypted SQLite database.\n\n" +
                    "• Bring-Your-Own-Key (BYOK): Your AI API keys are stored only inside the hardware-backed Android Keystore and never sent to our servers.\n\n" +
                    "• Private Google Drive: Cloud backups remain 100% inside your personal Google Drive account in a dedicated private vault.\n\n" +
                    "• Regulatory Compliance: Designed in full compliance with the Indian Digital Personal Data Protection (DPDP) Act.",
            confirmText = "Understood",
            onConfirm = { showPrivacyDialog = false },
            dismissText = null
        )
    }

    if (showTermsDialog) {
        AppleAlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = "Terms of Service",
            message = "PolicyDesk Advisor Edition Terms:\n\n" +
                    "• Professional Tool: PolicyDesk is an administrative management tool for insurance advisors, brokers, and agents.\n\n" +
                    "• Document Disclaimer: AI-extracted policy data should be verified before issuing official quotes or executing renewals.\n\n" +
                    "• 100% Data Ownership: You own all rights to your client portfolio and data. PolicyDesk does not claim or harvest any client data.\n\n" +
                    "• Offline Availability: The app operates without internet for record browsing, search, and client contact actions.",
            confirmText = "Accept",
            onConfirm = { showTermsDialog = false },
            dismissText = null
        )
    }

    if (showResetConfirmDialog) {
        AppleAlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = "Reset All Data",
            message = "Are you sure you want to delete all vehicles, policies, and client records from this device? This will erase your local portfolio database. This cannot be undone.",
            confirmText = "Delete Everything",
            isDestructive = true,
            onConfirm = {
                showResetConfirmDialog = false
                viewModel.resetAllData {
                    Toast.makeText(context, "All local records have been cleared", Toast.LENGTH_SHORT).show()
                }
            },
            dismissText = "Cancel",
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    if (state.showOtaDialog) {
        val otaState = state.otaState
        val release = when (otaState) {
            is OtaState.UpdateAvailable -> otaState.release
            is OtaState.Downloading -> otaState.release
            is OtaState.ReadyToInstall -> otaState.release
            else -> null
        }

        if (release != null) {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDownloading = otaState is OtaState.Downloading
            val isReady = otaState is OtaState.ReadyToInstall

            AppleAlertDialog(
                onDismissRequest = {
                    if (!isDownloading) viewModel.dismissOtaDialog()
                },
                title = "PolicyDesk ${release.tagName}",
                message = "A new software release is available for installation (${release.displaySizeMb}).",
                confirmText = when {
                    isReady -> "Install Now"
                    isDownloading -> "Downloading (${(otaState as OtaState.Downloading).progressPercent}%)"
                    else -> "Download & Install"
                },
                onConfirm = {
                    when {
                        isReady -> viewModel.installOtaUpdate()
                        isDownloading -> { /* in progress */ }
                        else -> viewModel.startOtaDownload()
                    }
                },
                dismissText = if (isDownloading) null else "Later",
                onDismiss = { viewModel.dismissOtaDialog() },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        if (isDownloading) {
                            val dl = otaState as OtaState.Downloading
                            Spacer(modifier = Modifier.height(6.dp))
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { dl.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                                trackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${dl.progressPercent}% (${String.format("%.1f", dl.downloadedBytes / (1024.0 * 1024.0))} MB / ${release.displaySizeMb})",
                                fontSize = 11.sp,
                                color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else if (release.releaseNotes.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x0A000000))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = release.releaseNotes,
                                    fontSize = 12.sp,
                                    color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Settings",
                subtitle = "Preferences, Profile & Compliance",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 0: APPEARANCE (TRUE DARK THEME)
            val currentThemeMode by ThemeManager.currentTheme.collectAsState()
            val haptic = LocalHapticFeedback.current

            LiquidGlassGroupedSection(title = "Appearance") {
                Text(
                    text = "Select your preferred visual style. True Dark uses pure OLED black for maximum contrast and battery conservation.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = labelSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { themeOption ->
                        val isSelected = currentThemeMode == themeOption
                        val pillShape = RoundedCornerShape(10.dp)
                        val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .background(
                                    if (isSelected) activeBlue else (if (isDark) Color(0x33545458) else Color(0x14767680)),
                                    pillShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) activeBlue else (if (isDark) Color(0x33FFFFFF) else Color(0x2E000000)),
                                    shape = pillShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    ThemeManager.setTheme(context, themeOption)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (themeOption) {
                                    AppTheme.DARK -> "True Dark"
                                    AppTheme.LIGHT -> "Light"
                                    AppTheme.SYSTEM -> "System"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else labelPrimary
                            )
                        }
                    }
                }
            }

            // SECTION 1: ADVISOR & AGENCY PROFILE
            LiquidGlassGroupedSection(title = "Advisor & Agency Profile") {
                Text(
                    text = "Personalize your business identity and customized WhatsApp renewal messages sent to clients.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = labelSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = advisorName,
                    onValueChange = { advisorName = it; profileSavedFeedback = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Advisor / Agent Name") },
                    placeholder = { Text("e.g. Satyam Jain") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = agencyName,
                    onValueChange = { agencyName = it; profileSavedFeedback = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Agency / Firm Name") },
                    placeholder = { Text("e.g. Jain Insurance Services") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = advisorPhone,
                    onValueChange = { advisorPhone = it; profileSavedFeedback = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Advisor Contact Number") },
                    placeholder = { Text("e.g. +91 9876543210") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "WHATSAPP RENEWAL MESSAGE TEMPLATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelSecondary,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Placeholders: {customer}, {vehicle}, {insurer}, {expiry}, {premium}, {signature}",
                    fontSize = 11.sp,
                    color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = if (customTemplate.isBlank()) AdvisorProfileManager.DEFAULT_TEMPLATE else customTemplate,
                    onValueChange = { customTemplate = it; profileSavedFeedback = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Message Template") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = formFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppleButton(
                        text = if (profileSavedFeedback) "Saved" else "Save Profile",
                        onClick = {
                            viewModel.saveAdvisorProfile(
                                AdvisorProfile(
                                    advisorName = advisorName.trim(),
                                    agencyName = agencyName.trim(),
                                    advisorPhone = advisorPhone.trim(),
                                    customWhatsappTemplate = customTemplate.trim()
                                )
                            )
                            profileSavedFeedback = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            Toast.makeText(context, "Advisor Profile Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1.2f)
                    )

                    AppleSecondaryButton(
                        text = "Reset Template",
                        onClick = {
                            customTemplate = AdvisorProfileManager.DEFAULT_TEMPLATE
                            profileSavedFeedback = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SECTION 2: SECURITY & APP LOCK
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
                        val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .background(if (isSelected) activeBlue else (if (isDark) Color(0x33545458) else Color(0x14767680)), pillShape)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) activeBlue else (if (isDark) Color(0x33FFFFFF) else Color(0x2E000000)),
                                    shape = pillShape
                                )
                                .clickable { viewModel.onProviderSelected(provider) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (provider) {
                                    AiProvider.CLOUDFLARE_AI -> "Cloudflare"
                                    AiProvider.GEMINI -> "Gemini"
                                    AiProvider.OPENAI -> "OpenAI"
                                    AiProvider.CLAUDE -> "Claude"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else labelPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isCloudflare = state.aiConfig.provider == AiProvider.CLOUDFLARE_AI

                if (isCloudflare) {
                    OutlinedTextField(
                        value = state.aiConfig.customEndpoint,
                        onValueChange = viewModel::onCustomEndpointChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Worker URL (Optional)") },
                        placeholder = { Text("https://policydesk-ai.satyamjain.workers.dev") },
                        supportingText = { Text("Zero configuration required. Free for all users.", color = AppleSystemGreen) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                } else {
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
                                    tint = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }

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
                    AiProvider.CLOUDFLARE_AI -> listOf("@cf/meta/llama-3.2-11b-vision-instruct")
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
                        enabled = (state.aiConfig.provider == AiProvider.CLOUDFLARE_AI || state.aiConfig.apiKey.isNotBlank()) && !state.isTestingAi,
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
                if (state.isDriveLinked) {
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
                                text = "Google Drive Linked & Active",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleSystemGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.googleAccountEmail ?: "Google Account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Vault: PolicyDesk Vault / policydesk_backup.json",
                                fontSize = 11.sp,
                                color = labelSecondary
                            )
                        }
                        androidx.compose.material3.TextButton(onClick = viewModel::disconnectGoogleDrive) {
                            Text("Disconnect", color = AppleSystemRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Total Records", value = "${state.totalRecords} vehicles")
                    DetailRow(label = "Cloud Vault", value = "PolicyDesk Vault")
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
                            text = "Restore from Vault",
                            onClick = viewModel::restoreFromDriveVault,
                            enabled = !state.isSyncing,
                            tintColor = labelPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleSecondaryButton(
                            text = "Save to Drive (Share)",
                            onClick = {
                                coroutineScope.launch {
                                    val shareUri = viewModel.getShareableBackupUri()
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Save to Google Drive"))
                                }
                            },
                            enabled = !state.isSyncing,
                            modifier = Modifier.weight(1.1f)
                        )

                        AppleSecondaryButton(
                            text = "Import JSON File",
                            onClick = { restoreSnapshotLauncher.launch(arrayOf("application/json", "*/*")) },
                            tintColor = labelPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        text = "Sign in with your Google Account to automatically sync all policies, vehicles, and customer records to a dedicated 'PolicyDesk Vault' on your Google Drive.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = labelSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow(label = "Storage", value = "Local-First Storage")
                    DetailRow(label = "Drive Status", value = "Not Linked")

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
                        text = "Sign in with Google Drive",
                        onClick = {
                            googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppleSecondaryButton(
                            text = "Save to Drive (Share)",
                            onClick = {
                                coroutineScope.launch {
                                    val shareUri = viewModel.getShareableBackupUri()
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Save directly to Google Drive"))
                                }
                            },
                            tintColor = AppleSystemBlue,
                            modifier = Modifier.weight(1.1f)
                        )

                        AppleSecondaryButton(
                            text = "Import JSON File",
                            onClick = { restoreSnapshotLauncher.launch(arrayOf("application/json", "*/*")) },
                            tintColor = labelPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppleSecondaryButton(
                    text = "Export Records to CSV",
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                AppleSecondaryButton(
                    text = "Recover Deleted Records",
                    onClick = viewModel::recoverLocalDeletedData,
                    icon = AppleIcons.History,
                    enabled = !state.isSyncing,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SECTION 5: REMINDERS & RENEWAL ALERTS
            LiquidGlassGroupedSection(title = "Reminders & Renewal Alerts") {
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
                                .background(AppleSystemOrange, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Policy Expiry Notifications", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = labelPrimary)
                            Text("Automated background reminders via WorkManager", fontSize = 12.sp, color = labelSecondary)
                        }
                    }
                    AppleSwitch(
                        checked = state.areNotificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppleSystemOrange.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, AppleSystemOrange.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "System notification permission is required for alerts.",
                                color = AppleSystemOrange,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) {
                                Text("Allow", color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DetailRow(label = "Expiry Cadence", value = "30d, 15d, 7d, 1d & same-day")
                DetailRow(label = "Daily Summary", value = "9:00 AM daily")
                DetailRow(label = "Reboot Recovery", value = "Auto-reschedules on boot")
                DetailRow(label = "Alert Engine", value = "WorkManager & NotificationManager")

                Spacer(modifier = Modifier.height(10.dp))

                AppleSecondaryButton(
                    text = "Send Test Notification",
                    onClick = {
                        viewModel.sendTestNotification()
                        Toast.makeText(context, "Test notification sent", Toast.LENGTH_SHORT).show()
                    },
                    icon = Icons.Default.Notifications,
                    tintColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SECTION 6: ABOUT & COMPLIANCE
            LiquidGlassGroupedSection(title = "About & Compliance") {
                DetailRow(label = "Application", value = "PolicyDesk Advisor")
                DetailRow(label = "Version", value = "v${state.appVersionName} (Build ${state.appVersionCode})")

                val otaState = state.otaState
                val otaSubtitle = when (otaState) {
                    is OtaState.Checking -> "Checking for updates..."
                    is OtaState.UpdateAvailable -> "Version ${otaState.release.tagName} available (${otaState.release.displaySizeMb})"
                    is OtaState.Downloading -> "Downloading update: ${otaState.progressPercent}%"
                    is OtaState.ReadyToInstall -> "Download complete • Tap to install ${otaState.release.tagName}"
                    is OtaState.UpToDate -> "You are on the latest version"
                    is OtaState.Error -> "Check failed: ${otaState.message}"
                    OtaState.Idle -> "Check GitHub for new releases"
                }
                val hasUpdate = otaState is OtaState.UpdateAvailable || otaState is OtaState.ReadyToInstall

                Spacer(modifier = Modifier.height(4.dp))
                LiquidGlassListRow(
                    title = if (hasUpdate) "Update Available" else "Software Updates",
                    subtitle = otaSubtitle,
                    icon = AppleIcons.Download,
                    iconBgColor = if (hasUpdate) com.policydesk.app.ui.theme.AppleDarkSystemGreen else (if (isDark) AppleDarkSystemBlue else AppleSystemBlue),
                    onClick = {
                        when (otaState) {
                            is OtaState.ReadyToInstall -> viewModel.installOtaUpdate()
                            is OtaState.UpdateAvailable -> viewModel.openOtaDialog()
                            is OtaState.Downloading -> viewModel.openOtaDialog()
                            else -> viewModel.checkForUpdates(manual = true)
                        }
                    }
                )

                DetailRow(label = "Built By", value = "The Software Labs & Satyam Jain")
                DetailRow(label = "Architecture", value = "100% Local-First & Encrypted")
                DetailRow(label = "Compliance", value = "Digital Personal Data Protection Act (DPDP)")

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassListRow(
                    title = "The Software Labs",
                    subtitle = "thesoftwarelabs.pages.dev • Built by The Software Labs & Satyam Jain",
                    icon = AppleIcons.House,
                    iconBgColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://thesoftwarelabs.pages.dev"))
                        context.startActivity(browserIntent)
                    }
                )

                LiquidGlassListRow(
                    title = "Share PolicyDesk App",
                    subtitle = "Recommend PolicyDesk to insurance colleagues",
                    icon = AppleIcons.Share,
                    iconBgColor = AppleSystemBlue,
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "PolicyDesk - Vehicle Insurance Advisor App")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "PolicyDesk: The offline-first vehicle policy and renewal tracking app for insurance advisors.\n" +
                                        "Automated WhatsApp reminders, AI document scanner, and Excel import.\n" +
                                        "Built by The Software Labs & Satyam Jain\n" +
                                        "https://thesoftwarelabs.pages.dev"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share PolicyDesk"))
                    }
                )

                LiquidGlassListRow(
                    title = "Support & Feedback",
                    subtitle = "Report issues or suggest features directly to developers",
                    icon = AppleIcons.Message,
                    iconBgColor = AppleSystemGreen,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@policydesk.app")
                            putExtra(Intent.EXTRA_SUBJECT, "PolicyDesk App Feedback [v${state.appVersionName}]")
                            putExtra(Intent.EXTRA_TEXT, "Device: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\n\nFeedback:")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                LiquidGlassListRow(
                    title = "Privacy Policy",
                    subtitle = "Local-first storage & zero data tracking commitments",
                    icon = AppleIcons.ShieldCheck,
                    iconBgColor = AppleSystemBlue,
                    onClick = { showPrivacyDialog = true }
                )

                LiquidGlassListRow(
                    title = "Terms of Service",
                    subtitle = "Advisor license, disclaimer & compliance standards",
                    icon = AppleIcons.DocText,
                    iconBgColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    showDivider = false,
                    onClick = { showTermsDialog = true }
                )
            }

            // SECTION 7: DATA MANAGEMENT & RESET
            LiquidGlassGroupedSection(title = "Data Management & Reset") {
                Text(
                    text = "Erase all local vehicle entries, policy records, and cached files from this device.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = labelSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                AppleSecondaryButton(
                    text = "Reset All Records",
                    onClick = { showResetConfirmDialog = true },
                    icon = AppleIcons.Trash,
                    tintColor = AppleSystemRed,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // BRANDING FOOTER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Built by The Software Labs & Satyam Jain",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = labelSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "thesoftwarelabs.pages.dev",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://thesoftwarelabs.pages.dev"))
                        context.startActivity(browserIntent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
