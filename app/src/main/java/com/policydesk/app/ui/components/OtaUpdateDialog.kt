package com.policydesk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.policydesk.app.data.ota.OtaManager
import com.policydesk.app.data.ota.OtaState
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary

/**
 * Apple iOS Software Update Modal
 * Premium frosted glass design with parsed clean changelog bullets,
 * version pill capsule, live download progress, and prominent action CTA.
 */
@Composable
fun OtaUpdateDialog(
    otaManager: OtaManager
) {
    val showDialog by otaManager.showOtaDialog.collectAsState()
    val otaState by otaManager.otaState.collectAsState()

    if (!showDialog) return

    val release = when (otaState) {
        is OtaState.UpdateAvailable -> (otaState as OtaState.UpdateAvailable).release
        is OtaState.Downloading -> (otaState as OtaState.Downloading).release
        is OtaState.ReadyToInstall -> (otaState as OtaState.ReadyToInstall).release
        else -> return
    }

    val isDownloading = otaState is OtaState.Downloading
    val isReady = otaState is OtaState.ReadyToInstall
    val isDark = isSystemInDarkTheme()

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val dialogBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF262629), Color(0xFF1B1B1D)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF7F7FA)))
    }

    val dialogBorder = if (isDark) Color(0x33FFFFFF) else Color(0x20000000)
    val cardBg = if (isDark) Color(0x18FFFFFF) else Color(0x0A000000)
    val cardBorder = if (isDark) Color(0x24FFFFFF) else Color(0x12000000)

    Dialog(
        onDismissRequest = {
            if (!isDownloading) otaManager.dismissOtaDialog()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(min = 320.dp, max = 390.dp)
                .fillMaxWidth()
                .shadow(28.dp, RoundedCornerShape(26.dp), spotColor = Color(0x60000000))
                .background(dialogBg, RoundedCornerShape(26.dp))
                .border(0.75.dp, dialogBorder, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon: Apple Software Update Squircle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF007AFF),
                                    Color(0xFF0051D5)
                                )
                            ),
                            RoundedCornerShape(15.dp)
                        )
                        .border(
                            0.75.dp,
                            Color.White.copy(alpha = 0.40f),
                            RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Download,
                        contentDescription = "Software Update",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "Software Update",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary,
                    letterSpacing = (-0.4).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version & Size Capsule Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(accentBlue.copy(alpha = if (isDark) 0.18f else 0.10f))
                        .border(0.5.dp, accentBlue.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Version ${release.versionName} • ${release.displaySizeMb}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentBlue,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Parsed Clean Changelog Card
                val changelog = release.parsedChangelog
                if (changelog.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "WHAT'S IN THIS UPDATE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = labelSecondary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 180.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            changelog.forEach { bullet ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp, end = 9.dp)
                                            .size(5.dp)
                                            .background(accentBlue, CircleShape)
                                    )
                                    Text(
                                        text = bullet,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = labelPrimary,
                                        lineHeight = 17.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Live Download Progress Bar (When downloading)
                if (isDownloading) {
                    val dl = otaState as OtaState.Downloading
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Downloading update...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                            Text(
                                text = "${dl.progressPercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentBlue,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { dl.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentBlue,
                            trackColor = accentBlue.copy(alpha = 0.18f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${String.format("%.1f", dl.downloadedBytes / (1024.0 * 1024.0))} MB of ${release.displaySizeMb}",
                            fontSize = 11.sp,
                            color = labelSecondary,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action Buttons
                val buttonText = when {
                    isReady -> "Install Now"
                    isDownloading -> "Downloading..."
                    else -> "Download & Install"
                }

                AppleButton(
                    text = buttonText,
                    onClick = {
                        when {
                            isReady -> {
                                (otaState as? OtaState.ReadyToInstall)?.apkFile?.let { otaManager.installUpdate(it) }
                            }
                            isDownloading -> { /* In progress */ }
                            else -> otaManager.startDownload()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 46.dp,
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isDownloading
                )

                if (!isDownloading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { otaManager.dismissOtaDialog() },
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Later",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelSecondary
                        )
                    }
                }
            }
        }
    }
}
