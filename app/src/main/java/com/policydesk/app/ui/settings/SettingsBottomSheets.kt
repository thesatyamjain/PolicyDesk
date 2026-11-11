package com.policydesk.app.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.data.drive.LocalSnapshotInfo
import com.policydesk.app.data.profile.AdvisorProfile
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.AppleSheetGrabber
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.detail.AccessibleStatusColors
import com.policydesk.app.ui.detail.rememberAnimatorsEnabled
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkTertiaryGrouped
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosTertiaryGrouped
import java.io.File

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    badge: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
                badge?.invoke()
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = labelSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x33787880) else Color(0x18787880))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                }
                .semantics { contentDescription = "Close $title" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppleIcons.Clear,
                contentDescription = null,
                tint = labelSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

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
    val focusManager = LocalFocusManager.current

    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val activeBlue = AccessibleStatusColors.blue(isDark)
    val activeGreen = AccessibleStatusColors.green(isDark)

    var advisorName by remember(initialProfile.advisorName) { mutableStateOf(initialProfile.advisorName) }
    var agencyName by remember(initialProfile.agencyName) { mutableStateOf(initialProfile.agencyName) }
    var advisorPhone by remember(initialProfile.advisorPhone) { mutableStateOf(initialProfile.advisorPhone) }
    var customTemplate by remember(initialProfile.customWhatsappTemplate) { mutableStateOf(initialProfile.customWhatsappTemplate) }
    var savedFeedback by remember { mutableStateOf(false) }

    val previewSignature = remember(advisorName, agencyName) {
        initialProfile.copy(
            advisorName = advisorName.trim(),
            agencyName = agencyName.trim()
        ).signature
    }
    val isCustomTemplate = customTemplate.isNotBlank() && customTemplate.trim() != AdvisorProfileManager.DEFAULT_TEMPLATE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { AppleSheetGrabber() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SheetHeader(
                title = "Advisor & Agency Profile",
                subtitle = "Customizes WhatsApp client updates and quote branding",
                onDismiss = onDismiss
            )

            if (initialProfile.advisorEmail.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(activeBlue.copy(alpha = 0.1f))
                        .border(1.dp, activeBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = AppleIcons.Checkmark,
                        contentDescription = null,
                        tint = activeBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Linked with Google: ${initialProfile.advisorEmail}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = activeBlue
                    )
                }
            }

            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = 2.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IDENTITY & SIGNATURE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelSecondary,
                            letterSpacing = 0.4.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = previewSignature,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedTextField(
                        value = advisorName,
                        onValueChange = { advisorName = it; savedFeedback = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Advisor / Agent Name") },
                        placeholder = { Text("e.g. Rajesh Sharma") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = agencyName,
                        onValueChange = { agencyName = it; savedFeedback = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Agency / Firm Name") },
                        placeholder = { Text("e.g. Shield Motor Advisory") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
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
                        textStyle = TextStyle(fontFeatureSettings = "tnum"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }
            }

            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = 2.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHATSAPP RENEWAL TEMPLATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelSecondary,
                            letterSpacing = 0.4.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background((if (isCustomTemplate) activeGreen else activeBlue).copy(alpha = 0.12f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isCustomTemplate) "Custom Active" else "Default Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCustomTemplate) activeGreen else activeBlue
                            )
                        }
                    }

                    Text(
                        text = "Placeholders: {customer}, {vehicle}, {insurer}, {expiry}, {premium}, {signature}",
                        fontSize = 11.sp,
                        color = activeBlue
                    )

                    OutlinedTextField(
                        value = if (customTemplate.isBlank()) AdvisorProfileManager.DEFAULT_TEMPLATE else customTemplate,
                        onValueChange = { customTemplate = it; savedFeedback = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Message Template") },
                        minLines = 3,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppleButton(
                    text = if (savedFeedback) "Profile Saved" else "Save Profile",
                    icon = if (savedFeedback) AppleIcons.Checkmark else null,
                    onClick = {
                        focusManager.clearFocus()
                        onSaveProfile(
                            initialProfile.copy(
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
                    tintColor = activeBlue,
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
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val animatorsEnabled = rememberAnimatorsEnabled()

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val labelTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val activeBlue = AccessibleStatusColors.blue(isDark)
    val activeGreen = AccessibleStatusColors.green(isDark)
    val activeOrange = AccessibleStatusColors.orange(isDark)
    val activeRed = AccessibleStatusColors.red(isDark)

    var showKey by remember { mutableStateOf(false) }
    var showCloudflareAdvanced by remember { mutableStateOf(false) }

    val isCloudflare = state.aiConfig.provider == AiProvider.CLOUDFLARE_AI
    val hasKey = state.aiConfig.apiKey.isNotBlank()
    val statusBadgeText = when {
        isCloudflare -> "EDGE ACTIVE"
        hasKey -> "KEY READY"
        else -> "KEY NEEDED"
    }
    val statusBadgeColor = if (isCloudflare || hasKey) activeGreen else activeOrange

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { AppleSheetGrabber() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SheetHeader(
                title = "AI Policy Extraction (BYOK)",
                subtitle = "Hardware Keystore protected • Zero cloud telemetry",
                onDismiss = onDismiss,
                badge = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBadgeColor.copy(alpha = 0.14f))
                            .border(0.75.dp, statusBadgeColor.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusBadgeColor
                        )
                    }
                }
            )

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
                        val providerLabel = when (provider) {
                            AiProvider.CLOUDFLARE_AI -> "Cloudflare"
                            AiProvider.GEMINI -> "Gemini"
                            AiProvider.OPENAI -> "OpenAI"
                            AiProvider.CLAUDE -> "Claude"
                        }
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
                                .clickable(role = Role.Tab) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.onProviderSelected(provider)
                                }
                                .semantics {
                                    selected = isSelected
                                    contentDescription = "$providerLabel AI Provider"
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = providerLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else labelPrimary
                            )
                        }
                    }
                }
            }

            if (isCloudflare) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 2.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(activeGreen)
                            )
                            Text(
                                text = "Edge AI Active — Ready to scan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeGreen
                            )
                        }
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
                        .clickable(role = Role.Button) { showCloudflareAdvanced = !showCloudflareAdvanced }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.aiConfig.customEndpoint.isNotBlank()) {
                            "Self-Host Endpoint: Custom"
                        } else {
                            "Self-Host / Custom Endpoint (Official Default)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelSecondary
                    )
                    Icon(
                        imageVector = if (showCloudflareAdvanced) AppleIcons.ChevronUp else AppleIcons.ChevronDown,
                        contentDescription = "Toggle Custom Endpoint",
                        tint = labelTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showCloudflareAdvanced,
                    enter = if (animatorsEnabled) androidx.compose.animation.fadeIn() else EnterTransition.None,
                    exit = if (animatorsEnabled) androidx.compose.animation.fadeOut() else ExitTransition.None
                ) {
                    OutlinedTextField(
                        value = state.aiConfig.customEndpoint,
                        onValueChange = viewModel::onCustomEndpointChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Worker URL (Self-Hosted)") },
                        placeholder = { Text("https://your-worker.workers.dev") },
                        supportingText = {
                            Text(
                                "Leave blank to use the official PolicyDesk Edge worker.",
                                color = labelTertiary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                }
            } else {
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeBlue.copy(alpha = 0.08f))
                            .border(1.dp, activeBlue.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { uriHandler.openUri(providerInfo.second) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = providerInfo.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeBlue
                            )
                            Text(
                                text = providerInfo.third,
                                fontSize = 11.sp,
                                color = labelSecondary
                            )
                        }
                        Icon(
                            imageVector = AppleIcons.ArrowUpRightSquare,
                            contentDescription = "Open external link",
                            tint = activeBlue,
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    imageVector = if (showKey) AppleIcons.EyeSlash else AppleIcons.Eye,
                                    contentDescription = if (showKey) "Hide API Key" else "Show API Key",
                                    tint = labelTertiary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )

                    AppleSecondaryButton(
                        text = "Paste",
                        icon = AppleIcons.DocOnDoc,
                        tintColor = activeBlue,
                        onClick = {
                            val clipText = clipboardManager.getText()?.text?.trim()
                            if (!clipText.isNullOrBlank()) {
                                viewModel.onApiKeyChanged(clipText)
                                Toast.makeText(context, "API Key pasted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(54.dp)
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
                                .background(activeBlue.copy(alpha = 0.12f))
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
                        enabled = !state.isFetchingModels && hasKey,
                        modifier = Modifier.size(28.dp)
                    ) {
                        if (state.isFetchingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = activeBlue
                            )
                        } else {
                            Icon(
                                imageVector = AppleIcons.ArrowClockwise,
                                contentDescription = "Refresh Models",
                                tint = if (hasKey) activeBlue else labelTertiary,
                                modifier = Modifier.size(16.dp)
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
                                        if (isSelected) activeBlue.copy(alpha = 0.14f)
                                        else if (isDark) IosDarkTertiaryGrouped
                                        else IosTertiaryGrouped
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) activeBlue else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(role = Role.Tab) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onModelChanged(modelOption)
                                    }
                                    .semantics { selected = isSelected }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = modelOption,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) activeBlue else labelSecondary
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
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.testAiConnection()
                    },
                    enabled = (isCloudflare || hasKey) && !state.isTestingAi,
                    isLoading = state.isTestingAi,
                    modifier = Modifier.weight(1f)
                )

                if (!isCloudflare && hasKey) {
                    AppleSecondaryButton(
                        text = "Remove",
                        onClick = viewModel::removeAiKey,
                        tintColor = activeRed,
                        icon = AppleIcons.Trash,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Test Feedback Banner
            state.aiTestMessage?.let { msg ->
                val isSuccess = state.aiTestSuccess
                val bannerColor = if (isSuccess) activeGreen else activeRed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            bannerColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            bannerColor.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = msg,
                        color = bannerColor,
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
    val haptic = LocalHapticFeedback.current

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val activeBlue = AccessibleStatusColors.blue(isDark)
    val activeGreen = AccessibleStatusColors.green(isDark)
    val activeOrange = AccessibleStatusColors.orange(isDark)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { AppleSheetGrabber() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SheetHeader(
                title = "Device Snapshots & Cloud Vault",
                subtitle = "Encrypted local snapshots & direct file restore",
                onDismiss = onDismiss,
                badge = {
                    val statusText = if (isSyncing) "SYNCING" else "${snapshots.size} READY"
                    val statusTint = if (isSyncing) activeOrange else activeGreen
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusTint.copy(alpha = 0.14f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTint,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            )

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
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    snapshots.forEachIndexed { index, snapshot ->
                        val isLatest = index == 0
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                            color = labelPrimary,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                        if (isLatest) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(activeGreen.copy(alpha = 0.14f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "LATEST",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = activeGreen
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${snapshot.recordCount} ${if (snapshot.recordCount == 1) "vehicle" else "vehicles"} • ${snapshot.fileSizeFormatted}",
                                        fontSize = 11.sp,
                                        color = labelSecondary,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(activeBlue.copy(alpha = 0.12f))
                                        .clickable(enabled = !isSyncing, role = Role.Button) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                }
            } else {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 1.dp
                ) {
                    Text(
                        text = "No local snapshots found yet. Snapshots are created automatically before major database actions.",
                        fontSize = 12.sp,
                        color = labelSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
