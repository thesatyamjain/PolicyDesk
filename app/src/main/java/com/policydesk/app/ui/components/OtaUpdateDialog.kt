package com.policydesk.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.data.ota.OtaManager
import com.policydesk.app.data.ota.OtaState
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelSecondary

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
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    AppleAlertDialog(
        onDismissRequest = {
            if (!isDownloading) otaManager.dismissOtaDialog()
        },
        title = "PolicyDesk ${release.tagName}",
        message = "A new software release is available for installation (${release.displaySizeMb}).",
        confirmText = when {
            isReady -> "Install Now"
            isDownloading -> "Downloading ${(otaState as OtaState.Downloading).progressPercent}%"
            else -> "Download & Install"
        },
        onConfirm = {
            when {
                isReady -> {
                    (otaState as? OtaState.ReadyToInstall)?.apkFile?.let { otaManager.installUpdate(it) }
                }
                isDownloading -> { /* in progress */ }
                else -> otaManager.startDownload()
            }
        },
        dismissText = if (isDownloading) null else "Later",
        onDismiss = { otaManager.dismissOtaDialog() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (release.releaseNotes.isNotBlank()) {
                    Text(
                        text = "What's New:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = release.releaseNotes.take(300),
                        fontSize = 11.sp,
                        color = labelSecondary,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isDownloading) {
                    val dl = otaState as OtaState.Downloading
                    LinearProgressIndicator(
                        progress = { dl.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AppleSystemBlue,
                        trackColor = AppleSystemBlue.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${dl.progressPercent}% (${String.format("%.1f", dl.downloadedBytes / (1024.0 * 1024.0))} MB / ${release.displaySizeMb})",
                        fontSize = 11.sp,
                        color = labelSecondary
                    )
                }
            }
        }
    )
}
