package com.policydesk.app.ui.settings

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.ota.OtaState
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.data.theme.AppTheme
import com.policydesk.app.data.theme.ThemeManager
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleSwitch
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassGroupedSection
import com.policydesk.app.ui.components.LiquidGlassListRow
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemPurple
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemPurple
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

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

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshNotificationPermission()
    }

    // Modal Sheet & Dialog Presentation States
    var showProfileSheet by remember { mutableStateOf(false) }
    var showAiConfigSheet by remember { mutableStateOf(false) }
    var showSnapshotsSheet by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Dialogs
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
            message = "Are you sure you want to delete all vehicles, policies, and client records from this device? This will permanently erase your local portfolio database. This cannot be undone.",
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
            val isDark = isSystemInDarkTheme()
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
        val labelTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
        val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

        val avatarBitmap = remember(state.avatarUpdateTimestamp, state.googleAccountPhotoUrl, state.isDriveLinked) {
            if (state.isDriveLinked) {
                val file = File(context.filesDir, "google_avatar.png")
                if (file.exists() && file.length() > 0) {
                    try {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                } else null
            } else null
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // TOP IDENTITY HERO BANNER (Samsung One UI / Apple ID Spec)
            // ==========================================
            if (!state.isDriveLinked) {
                // SIGNED OUT: PROMINENT ONE UI HERO CALLOUT
                LiquidGlassCard(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = "Sign in to Google Drive",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sync policies, clients & encrypted cloud vault.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = activeBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Circular soft-blue avatar with person silhouette
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(activeBlue.copy(alpha = 0.14f))
                                .border(1.dp, activeBlue.copy(alpha = 0.28f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.Person,
                                contentDescription = "Sign in to Google Drive",
                                tint = activeBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            } else {
                // SIGNED IN: ACTIVE VAULT IDENTITY BANNER WITH GOOGLE AVATAR
                val accountName = state.googleAccountDisplayName?.takeIf { it.isNotBlank() }
                    ?: state.advisorProfile.advisorName.takeIf { it.isNotBlank() }
                    ?: state.googleAccountEmail?.substringBefore("@")
                    ?: "PolicyDesk Advisor"

                LiquidGlassCard(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.triggerSyncNow()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = accountName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary,
                                    letterSpacing = (-0.3).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                // Active Vault Badge Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(AppleSystemGreen.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                        .border(1.dp, AppleSystemGreen.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(AppleSystemGreen, CircleShape)
                                    )
                                    Text(
                                        text = "ACTIVE VAULT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleSystemGreen,
                                        letterSpacing = 0.4.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = state.googleAccountEmail ?: "Personal Drive Vault",
                                fontSize = 13.sp,
                                color = labelSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (state.isSyncing) "Syncing with Google Drive..." else "${state.totalRecords} vehicles synced • Tap to sync now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (state.isSyncing) AppleSystemOrange else activeBlue
                            )
                        }

                        // Avatar container with live photo or monogram fallback
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Google Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, if (isDark) Color(0x40FFFFFF) else Color(0x24000000), CircleShape)
                                )
                            } else {
                                val monogram = accountName.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    activeBlue,
                                                    Color(0xFF5AC8FA)
                                                )
                                            )
                                        )
                                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = monogram,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Active green online dot at bottom-end corner
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(AppleSystemGreen, CircleShape)
                                    .border(2.dp, if (isDark) Color(0xFF1C1C1E) else Color.White, CircleShape)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // GROUP 1: PREFERENCES & IDENTITY
            // ==========================================
            val currentThemeMode by ThemeManager.currentTheme.collectAsState()

            LiquidGlassGroupedSection(title = "Preferences") {
                // Interactive 3-Segment Theme Switcher Pill
                Text(
                    text = "VISUAL APPEARANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelSecondary,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { themeOption ->
                        val isSelected = currentThemeMode == themeOption
                        val pillShape = RoundedCornerShape(10.dp)

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

                Spacer(modifier = Modifier.height(10.dp))

                // Advisor & Agency Profile Row
                val advisorDisplayName = state.advisorProfile.advisorName.ifBlank { "Personalize identity" }
                val agencySubtext = if (state.advisorProfile.agencyName.isNotBlank()) {
                    "${state.advisorProfile.agencyName} • WhatsApp templates & branding"
                } else {
                    "Agency name, phone & WhatsApp templates"
                }

                LiquidGlassListRow(
                    title = "Advisor & Agency Profile",
                    subtitle = agencySubtext,
                    value = advisorDisplayName.takeIf { it != "Personalize identity" },
                    icon = AppleIcons.Person2,
                    iconBgColor = AppleSystemBlue,
                    showDivider = false,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showProfileSheet = true }
                )
            }

            // ==========================================
            // GROUP 2: INTELLIGENCE & EXTRACTION
            // ==========================================
            LiquidGlassGroupedSection(title = "Intelligence & Extraction") {
                val isCloudflare = state.aiConfig.provider == AiProvider.CLOUDFLARE_AI
                val hasApiKey = state.aiConfig.apiKey.isNotBlank()
                val activeModelName = state.aiConfig.model.ifBlank { state.aiConfig.provider.defaultModel }

                LiquidGlassListRow(
                    title = "AI Policy Extraction (BYOK)",
                    subtitle = "${state.aiConfig.provider.displayName} • $activeModelName",
                    icon = Icons.Default.Psychology,
                    iconBgColor = if (isDark) AppleDarkSystemPurple else AppleSystemPurple,
                    showDivider = false,
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isCloudflare || hasApiKey) AppleSystemGreen.copy(alpha = 0.14f)
                                        else AppleSystemOrange.copy(alpha = 0.14f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCloudflare || hasApiKey) AppleSystemGreen.copy(alpha = 0.35f)
                                        else AppleSystemOrange.copy(alpha = 0.35f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCloudflare) "EDGE READY" else if (hasApiKey) "ACTIVE" else "SETUP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCloudflare || hasApiKey) AppleSystemGreen else AppleSystemOrange
                                )
                            }
                            Icon(
                                imageVector = AppleIcons.ChevronRight,
                                contentDescription = null,
                                tint = labelTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = { showAiConfigSheet = true }
                )
            }

            // ==========================================
            // GROUP 3: CLOUD & PORTFOLIO BACKUPS
            // ==========================================
            LiquidGlassGroupedSection(title = "Cloud & Data Vault") {
                // Google Drive Sync Row
                LiquidGlassListRow(
                    title = "Google Drive Cloud Vault",
                    subtitle = if (state.isDriveLinked) {
                        "${state.googleAccountEmail} • Synced: ${state.lastSyncTime}"
                    } else {
                        "Sync encrypted backups to personal Google Drive"
                    },
                    icon = Icons.Default.CloudDone,
                    iconBgColor = if (state.isDriveLinked) AppleSystemGreen else AppleSystemBlue,
                    trailingContent = {
                        if (state.isDriveLinked) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = activeBlue
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(activeBlue.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Sync Now",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = activeBlue
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Connect",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeBlue
                                )
                                Icon(
                                    imageVector = AppleIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = labelTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        if (state.isDriveLinked) {
                            viewModel.triggerSyncNow()
                        } else {
                            googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                        }
                    }
                )

                // Device Rolling Snapshots Row
                LiquidGlassListRow(
                    title = "Device Rolling Snapshots",
                    subtitle = "${state.localSnapshots.size} offline rollback points available",
                    icon = AppleIcons.History,
                    iconBgColor = AppleSystemOrange,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showSnapshotsSheet = true }
                )

                // Export Portfolio to CSV
                LiquidGlassListRow(
                    title = "Export Portfolio to CSV",
                    subtitle = "Export all vehicle & policy records as spreadsheet",
                    icon = AppleIcons.TableCells,
                    iconBgColor = AppleSystemGreen,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.Share,
                            contentDescription = "Export CSV",
                            tint = activeBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    },
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
                    }
                )

                // Recover Deleted Records
                LiquidGlassListRow(
                    title = "Recover Deleted Records",
                    subtitle = "Access deleted vehicle archive and restore",
                    icon = AppleIcons.Trash,
                    iconBgColor = Color(0xFF8E8E93),
                    showDivider = false,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = viewModel::recoverLocalDeletedData
                )
            }

            // ==========================================
            // GROUP 4: NOTIFICATIONS & SECURITY
            // ==========================================
            LiquidGlassGroupedSection(title = "Notifications & Security") {
                // Policy Expiry Alerts Row
                LiquidGlassListRow(
                    title = "Policy Expiry Alerts",
                    subtitle = "Daily background renewal checks at 9:00 AM",
                    icon = Icons.Default.Notifications,
                    iconBgColor = AppleSystemOrange,
                    trailingContent = {
                        AppleSwitch(
                            checked = state.areNotificationsEnabled,
                            onCheckedChange = viewModel::toggleNotifications
                        )
                    }
                )

                // Notification Permission Warning Banner (if denied on API 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.hasNotificationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppleSystemOrange.copy(alpha = if (isDark) 0.14f else 0.08f))
                            .border(0.5.dp, AppleSystemOrange.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Permission required for expiry alerts",
                                fontSize = 12.sp,
                                color = labelPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppleSystemOrange.copy(alpha = 0.20f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Allow",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleSystemOrange
                                )
                            }
                        }
                    }
                }

                // App Lock & Biometrics Row
                LiquidGlassListRow(
                    title = "App Lock & Biometrics",
                    subtitle = "Require PIN or biometric authentication on app launch",
                    icon = AppleIcons.Lock,
                    iconBgColor = AppleSystemBlue,
                    showDivider = false,
                    trailingContent = {
                        AppleSwitch(
                            checked = state.isAppLockEnabled,
                            onCheckedChange = viewModel::toggleAppLock
                        )
                    }
                )
            }

            // ==========================================
            // GROUP 5: ABOUT & COMPLIANCE
            // ==========================================
            val otaState = state.otaState
            val otaSubtitle = when (otaState) {
                is OtaState.Checking -> "Checking for updates..."
                is OtaState.UpdateAvailable -> "Version ${otaState.release.tagName} available (${otaState.release.displaySizeMb})"
                is OtaState.Downloading -> "Downloading update: ${otaState.progressPercent}%"
                is OtaState.ReadyToInstall -> "Download complete • Tap to install ${otaState.release.tagName}"
                is OtaState.UpToDate -> "Up to date (Build ${state.appVersionCode})"
                is OtaState.Error -> "Check failed: ${otaState.message}"
                OtaState.Idle -> "Tap to check for new releases"
            }
            val hasUpdate = otaState is OtaState.UpdateAvailable || otaState is OtaState.ReadyToInstall

            LiquidGlassGroupedSection(title = "About & Compliance") {
                // Software Updates Row
                LiquidGlassListRow(
                    title = if (hasUpdate) "Update Available" else "Software Updates",
                    subtitle = otaSubtitle,
                    value = "v${state.appVersionName}",
                    icon = AppleIcons.Download,
                    iconBgColor = if (hasUpdate) AppleDarkSystemGreen else (if (isDark) AppleDarkSystemBlue else AppleSystemBlue),
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        when (otaState) {
                            is OtaState.ReadyToInstall -> viewModel.installOtaUpdate()
                            is OtaState.UpdateAvailable -> viewModel.openOtaDialog()
                            is OtaState.Downloading -> viewModel.openOtaDialog()
                            else -> viewModel.checkForUpdates(manual = true)
                        }
                    }
                )

                // The Software Labs
                LiquidGlassListRow(
                    title = "The Software Labs",
                    subtitle = "thesoftwarelabs.pages.dev",
                    icon = AppleIcons.House,
                    iconBgColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://thesoftwarelabs.pages.dev"))
                        context.startActivity(browserIntent)
                    }
                )

                // Share PolicyDesk App
                LiquidGlassListRow(
                    title = "Share PolicyDesk App",
                    subtitle = "Recommend PolicyDesk to insurance colleagues",
                    icon = AppleIcons.Share,
                    iconBgColor = AppleSystemBlue,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
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

                // Support & Feedback
                LiquidGlassListRow(
                    title = "Support & Feedback",
                    subtitle = "Report issues or suggest features directly to developers",
                    icon = AppleIcons.Message,
                    iconBgColor = AppleSystemGreen,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
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

                // Privacy Policy
                LiquidGlassListRow(
                    title = "Privacy Policy",
                    subtitle = "Local-first storage & DPDP Act compliance commitments",
                    icon = AppleIcons.ShieldCheck,
                    iconBgColor = AppleSystemBlue,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showPrivacyDialog = true }
                )

                // Terms of Service
                LiquidGlassListRow(
                    title = "Terms of Service",
                    subtitle = "Advisor license, disclaimer & compliance standards",
                    icon = AppleIcons.DocText,
                    iconBgColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    showDivider = false,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showTermsDialog = true }
                )
            }

            // ==========================================
            // GROUP 6: DANGER ZONE
            // ==========================================
            LiquidGlassGroupedSection(title = "Danger Zone") {
                LiquidGlassListRow(
                    title = "Reset All Records",
                    subtitle = "Permanently erase local portfolio database and cached keys",
                    icon = AppleIcons.Trash,
                    iconBgColor = AppleSystemRed,
                    showDivider = false,
                    trailingContent = {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = AppleSystemRed,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showResetConfirmDialog = true }
                )
            }

            // ==========================================
            // BRANDING FOOTER
            // ==========================================
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
                    color = activeBlue,
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

    // ==========================================
    // MODAL BOTTOM SHEETS
    // ==========================================
    SettingsAdvisorProfileSheet(
        isOpen = showProfileSheet,
        onDismiss = { showProfileSheet = false },
        initialProfile = state.advisorProfile,
        onSaveProfile = { newProfile ->
            viewModel.saveAdvisorProfile(newProfile)
        }
    )

    SettingsAiConfigSheet(
        isOpen = showAiConfigSheet,
        onDismiss = { showAiConfigSheet = false },
        state = state,
        viewModel = viewModel
    )

    SettingsVaultSnapshotsSheet(
        isOpen = showSnapshotsSheet,
        onDismiss = { showSnapshotsSheet = false },
        snapshots = state.localSnapshots,
        isSyncing = state.isSyncing,
        onRestoreSnapshot = { file ->
            viewModel.restoreFromLocalSnapshot(file)
            showSnapshotsSheet = false
        },
        onSaveShareBackup = {
            coroutineScope.launch {
                val shareUri = viewModel.getShareableBackupUri()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Save to Google Drive / Files"))
            }
        },
        onBrowseFile = {
            restoreSnapshotLauncher.launch(arrayOf("application/json", "*/*"))
        }
    )
}
