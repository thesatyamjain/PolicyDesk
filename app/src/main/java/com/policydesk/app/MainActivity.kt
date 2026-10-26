package com.policydesk.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.LiquidGlassCanvas
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.theme.AppleIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.policydesk.app.data.security.AppLockManager
import com.policydesk.app.ui.navigation.PolicyDeskNavHost
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.PolicyDeskTheme
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPdfUri: android.net.Uri? = if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "application/pdf") {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM)
            }
        } else {
            null
        }

        val openRegNo = intent?.getStringExtra("open_reg_no")
        val lockEnabled = AppLockManager.isAppLockEnabled(this)

        setContent {
            PolicyDeskTheme {
                LiquidGlassCanvas {
                    var isUnlocked by remember { mutableStateOf(!lockEnabled) }

                    if (isUnlocked) {
                        PolicyDeskNavHost(
                            initialPdfUri = sharedPdfUri,
                            initialRegNo = openRegNo
                        )
                    } else {
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            AppLockManager.showLockPrompt(
                                activity = this@MainActivity,
                                onSuccess = { isUnlocked = true },
                                onFailure = { /* stay locked */ }
                            )
                        }

                        AppLockScreen(
                            onUnlockClicked = {
                                AppLockManager.showLockPrompt(
                                    activity = this@MainActivity,
                                    onSuccess = { isUnlocked = true },
                                    onFailure = { /* stay locked */ }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun AppLockScreen(onUnlockClicked: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val labelPrimary = if (isDark) com.policydesk.app.ui.theme.IosDarkLabelPrimary else com.policydesk.app.ui.theme.IosLabelPrimary
    val labelSecondary = if (isDark) com.policydesk.app.ui.theme.IosDarkLabelSecondary else com.policydesk.app.ui.theme.IosLabelSecondary

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = androidx.compose.ui.Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            elevation = 6.dp
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(68.dp)
                        .background(SapphireBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Lock,
                        contentDescription = null,
                        tint = SapphireBlue,
                        modifier = androidx.compose.ui.Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = androidx.compose.ui.Modifier.height(18.dp))
                Text(
                    text = "PolicyDesk is Locked",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                Text(
                    text = "Authentication required to access customer policy records.",
                    fontSize = 13.sp,
                    color = labelSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                AppleButton(
                    text = "Unlock with Biometrics / PIN",
                    onClick = onUnlockClicked,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                )
            }
        }
    }
}
