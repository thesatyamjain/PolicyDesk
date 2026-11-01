package com.policydesk.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.data.drive.LocalSnapshotInfo
import com.policydesk.app.data.profile.AdvisorProfile
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkTertiaryGrouped
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosTertiaryGrouped
import java.io.File

/**
 * Dedicated Bottom Sheet for Advisor & Firm Profile Configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdvisorProfileSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    initialProfile: AdvisorProfile,
    onSaveProfile: (AdvisorProfile) -> Unit
) {
    if (!isOpen) return
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    var advisorName by remember(initialProfile.advisorName) { mutableStateOf(initialProfile.advisorName) }
    var agencyName by remember(initialProfile.agencyName) { mutableStateOf(initialProfile.agencyName) }
    var advisorPhone by remember(initialProfile.advisorPhone) { mutableStateOf(initialProfile.advisorPhone) }
    var customTemplate by remember(initialProfile.customWhatsappTemplate) { mutableStateOf(initialProfile.customWhatsappTemplate) }
    var savedFeedback by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "Advisor & Agency Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Customizes WhatsApp client updates and quote branding",
                    fontSize = 13.sp,
                    color = labelSecondary
                )
            }

            OutlinedTextField(
                value = advisorName,
                onValueChange = { advisorName = it; savedFeedback = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Advisor / Agent Name") },
                placeholder = { Text("e.g. Satyam Jain") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            OutlinedTextField(
                value = agencyName,
                onValueChange = { agencyName = it; savedFeedback = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Agency / Firm Name") },
                placeholder = { Text("e.g. Jain Insurance Services") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            OutlinedTextField(
                value = advisorPhone,
                onValueChange = { advisorPhone = it; savedFeedback = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Advisor Contact Number") },
                placeholder = { Text("e.g. +91 9876543210") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            Column {
                Text(
                    text = "WHATSAPP RENEWAL MESSAGE TEMPLATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelSecondary,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Placeholders: {customer}, {vehicle}, {insurer}, {expiry}, {premium}, {signature}",
                    fontSize = 11.sp,
                    color = activeBlue
                )
            }

            OutlinedTextField(
                value = if (customTemplate.isBlank()) AdvisorProfileManager.DEFAULT_TEMPLATE else customTemplate,
                onValueChange = { customTemplate = it; savedFeedback = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Message Template") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleButton(
                    text = if (savedFeedback) "Saved ✓" else "Save Profile",
                    onClick = {
                        onSaveProfile(
                            AdvisorProfile(
                                advisorName = advisorName.trim(),
                                agencyName = agencyName.trim(),
                                advisorPhone = advisorPhone.trim(),
                                customWhatsappTemplate = customTemplate.trim()
                            )
                        )
                        savedFeedback = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        Toast.makeText(context, "Advisor Profile Saved", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )

                AppleSecondaryButton(
                    text = "Reset Template",
                    onClick = {
                        customTemplate = AdvisorProfileManager.DEFAULT_TEMPLATE
                        savedFeedback = false
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Dedicated Bottom Sheet for AI Extraction Engine & BYOK Settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAiConfigSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    state: SettingsUiState,
    viewModel: SettingsViewModel
) {
    if (!isOpen) return
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    var showKey by remember { mutableStateOf(false) }
    var showCloudflareAdvanced by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "AI Policy Extraction (BYOK)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hardware Keystore protected • Zero cloud telemetry",
                    fontSize = 13.sp,
                    color = labelSecondary
                )
            }

            // Provider selection pills
            Column {
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
                                .background(
                                    if (isSelected) activeBlue else (if (isDark) Color(0x33545458) else Color(0x14767680)),
                                    pillShape
                                )
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
            }

            val isCloudflare = state.aiConfig.provider == AiProvider.CLOUDFLARE_AI

            if (isCloudflare) {
                // Cloudflare Zero-Clutter Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) AppleSystemGreen.copy(alpha = 0.12f) else AppleSystemGreen.copy(alpha = 0.08f))
                        .border(1.dp, AppleSystemGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AppleSystemGreen)
                            )
                            Text(
                                text = "Edge AI Active / Ready to scan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleSystemGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Runs directly on serverless edge (Meta Llama 3.2 11B Vision). No API key, developer account, or credit card required.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = labelSecondary
                        )
                    }
                }

                // Advanced / Self-Host toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCloudflareAdvanced = !showCloudflareAdvanced }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Self-Host / Custom Endpoint",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelSecondary
                    )
                    Icon(
                        imageVector = if (showCloudflareAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Custom Endpoint",
                        tint = if (isDark) IosDarkLabelTertiary else IosLabelTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showCloudflareAdvanced) {
                    OutlinedTextField(
                        value = state.aiConfig.customEndpoint,
                        onValueChange = viewModel::onCustomEndpointChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Worker URL (Self-Hosted)") },
                        placeholder = { Text("https://policydesk-ai.satyamjain.workers.dev") },
                        supportingText = { Text("Leave blank to use official PolicyDesk Edge.", color = if (isDark) IosDarkLabelTertiary else IosLabelTertiary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }
            } else {
                // Get Key Helper Link
                val providerInfo = when (state.aiConfig.provider) {
                    AiProvider.GEMINI -> Triple(
                        "Get key from Google AI Studio",
                        "https://aistudio.google.com/apikey",
                        "Free tier with instant setup using your Google account"
                    )
                    AiProvider.OPENAI -> Triple(
                        "Get key from OpenAI platform",
                        "https://platform.openai.com/api-keys",
                        "Requires OpenAI account with API credits"
                    )
                    AiProvider.CLAUDE -> Triple(
                        "Get key from Anthropic console",
                        "https://console.anthropic.com/settings/keys",
                        "Requires an Anthropic developer account"
                    )
                    else -> Triple("", "", "")
                }

                if (providerInfo.first.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppleSystemBlue.copy(alpha = 0.08f))
                            .border(1.dp, AppleSystemBlue.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                            .clickable { uriHandler.openUri(providerInfo.second) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = providerInfo.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleSystemBlue
                            )
                            Text(
                                text = providerInfo.third,
                                fontSize = 11.sp,
                                color = labelSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = AppleSystemBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // API Key field with paste
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.aiConfig.apiKey,
                        onValueChange = viewModel::onApiKeyChanged,
                        modifier = Modifier.weight(1f),
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

                    AppleSecondaryButton(
                        text = "Paste",
                        icon = Icons.Default.ContentPaste,
                        onClick = {
                            val clipText = clipboardManager.getText()?.text?.trim()
                            if (!clipText.isNullOrBlank()) {
                                viewModel.onApiKeyChanged(clipText)
                                Toast.makeText(context, "API Key pasted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    )
                }

                // Active Model Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Active Model",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelPrimary
                        )
                        val activeModelName = state.aiConfig.model.ifBlank { state.aiConfig.provider.defaultModel }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AppleSystemBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeModelName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = viewModel::refreshLiveModels,
                        enabled = !state.isFetchingModels && state.aiConfig.apiKey.isNotBlank(),
                        modifier = Modifier.size(24.dp)
                    ) {
                        if (state.isFetchingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = activeBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Models",
                                tint = if (state.aiConfig.apiKey.isNotBlank()) activeBlue else (if (isDark) IosDarkLabelTertiary else IosLabelTertiary),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                val baseModelsList = state.availableModels.ifEmpty {
                    when (state.aiConfig.provider) {
                        AiProvider.GEMINI -> listOf("gemini-3.5-flash-lite", "gemini-3.8-flash", "gemini-2.5-flash")
                        AiProvider.OPENAI -> listOf("gpt-4o-mini", "gpt-4o", "o1-mini")
                        AiProvider.CLAUDE -> listOf("claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022", "claude-3-opus-20240229")
                        else -> emptyList()
                    }
                }
                val modelsList = if (state.aiConfig.model.isNotBlank() && !baseModelsList.any { it.equals(state.aiConfig.model, ignoreCase = true) }) {
                    listOf(state.aiConfig.model) + baseModelsList
                } else {
                    baseModelsList
                }

                if (modelsList.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(modelsList) { modelOption ->
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
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onModelChanged(modelOption)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = modelOption,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) AppleSystemBlue else labelSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Test Connection & Remove Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppleButton(
                    text = if (state.isTestingAi) "Testing..." else "Test Connection",
                    onClick = viewModel::testAiConnection,
                    enabled = (state.aiConfig.provider == AiProvider.CLOUDFLARE_AI || state.aiConfig.apiKey.isNotBlank()) && !state.isTestingAi,
                    isLoading = state.isTestingAi,
                    modifier = Modifier.weight(1f)
                )

                if (!isCloudflare && state.aiConfig.apiKey.isNotBlank()) {
                    AppleSecondaryButton(
                        text = "Remove",
                        onClick = viewModel::removeAiKey,
                        tintColor = AppleSystemRed,
                        icon = Icons.Default.Delete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Test Feedback Banner
            state.aiTestMessage?.let { msg ->
                val isSuccess = state.aiTestSuccess
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSuccess) AppleSystemGreen.copy(alpha = 0.12f) else AppleSystemRed.copy(alpha = 0.12f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSuccess) AppleSystemGreen.copy(alpha = 0.35f) else AppleSystemRed.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
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
    }
}

/**
 * Dedicated Bottom Sheet for Device Rolling Snapshots & Local Vault Backups
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsVaultSnapshotsSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    snapshots: List<LocalSnapshotInfo>,
    isSyncing: Boolean,
    onRestoreSnapshot: (File) -> Unit,
    onSaveShareBackup: () -> Unit,
    onBrowseFile: () -> Unit
) {
    if (!isOpen) return
    val isDark = isSystemInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = "Device Snapshots & Cloud Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Encrypted local snapshots & direct file restore",
                    fontSize = 13.sp,
                    color = labelSecondary
                )
            }

            // Quick actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppleSecondaryButton(
                    text = "Save / Share Backup",
                    onClick = onSaveShareBackup,
                    tintColor = activeBlue,
                    modifier = Modifier.weight(1f)
                )

                AppleSecondaryButton(
                    text = "Browse Drive / File",
                    onClick = onBrowseFile,
                    tintColor = activeBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            // Snapshots list
            if (snapshots.isNotEmpty()) {
                Text(
                    text = "PROTECTED ROLLING SNAPSHOTS (${snapshots.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelSecondary,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snapshots.forEachIndexed { index, snapshot ->
                        val isLatest = index == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF2C2C2E) else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isLatest) AppleSystemGreen.copy(alpha = 0.35f) else if (isDark) Color(0x22FFFFFF) else Color(0x14000000),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = snapshot.timestampFormatted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = labelPrimary
                                    )
                                    if (isLatest) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AppleSystemGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "LATEST",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppleSystemGreen
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${snapshot.recordCount} vehicles • ${snapshot.fileSizeFormatted}",
                                    fontSize = 11.sp,
                                    color = labelSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .background(
                                        activeBlue.copy(alpha = 0.12f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !isSyncing) {
                                        onRestoreSnapshot(snapshot.file)
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Restore",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeBlue
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No local snapshots found yet. Snapshots are created automatically before major database actions.",
                    fontSize = 12.sp,
                    color = labelSecondary
                )
            }
        }
    }
}
