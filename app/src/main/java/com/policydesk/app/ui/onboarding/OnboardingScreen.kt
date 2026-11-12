package com.policydesk.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.policydesk.app.R
import com.policydesk.app.data.onboarding.OnboardingManager
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.detail.AccessibleStatusColors
import com.policydesk.app.ui.detail.rememberAnimatorsEnabled
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val features: List<String>
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val animatorsEnabled = rememberAnimatorsEnabled()

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val accentIndigo = if (isDark) Color(0xFF7C7AF2) else Color(0xFF4F46E5)
    val accentGreen = AccessibleStatusColors.green(isDark)

    val pages = remember(isDark) {
        listOf(
            OnboardingPageData(
                title = "Smart Vehicle Portfolio",
                subtitle = "Manage your entire client vehicle book with live expiry countdowns, status badges, and zero paper chaos.",
                icon = AppleIcons.Car,
                accentColor = accentBlue,
                features = listOf(
                    "Real-time expiry countdowns (Active, Expiring, Overdue)",
                    "HSRP number plates for Private, Commercial, and EV fleets",
                    "Fast search by RC, customer name, or mobile number"
                )
            ),
            OnboardingPageData(
                title = "Zero-Effort PDF Intake",
                subtitle = "Share or open any insurer policy PDF into PolicyDesk. Intelligent extraction fills policy and vehicle data automatically.",
                icon = AppleIcons.Brain,
                accentColor = accentIndigo,
                features = listOf(
                    "Automatic bilingual and OCR label cleaning",
                    "Customer names formatted cleanly to Title Case",
                    "Bulk Excel and CSV spreadsheet portfolio import"
                )
            ),
            OnboardingPageData(
                title = "Instant Client Renewals",
                subtitle = "Send personalized renewal notices on WhatsApp before policies lapse with your branded advisor signature.",
                icon = AppleIcons.WhatsApp,
                accentColor = accentGreen,
                features = listOf(
                    "One-tap pre-filled WhatsApp renewal messages",
                    "Customizable message template with your contact details",
                    "Offline-first with encrypted Google Drive auto-sync"
                )
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

    // Optional quick profile setup on slide 3
    val existingProfile = remember { AdvisorProfileManager.getProfile(context) }
    var advisorName by remember { mutableStateOf(existingProfile.advisorName) }
    var agencyName by remember { mutableStateOf(existingProfile.agencyName) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onFinish()
    }

    fun completeOnboarding(requestNotification: Boolean = true) {
        focusManager.clearFocus()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (advisorName.isNotBlank() || agencyName.isNotBlank()) {
            val formattedName = FieldValidators.formatNameTitleCase(advisorName)
            val updated = existingProfile.copy(
                advisorName = formattedName,
                agencyName = agencyName.trim()
            )
            AdvisorProfileManager.saveProfile(context, updated)
        }
        OnboardingManager.setOnboardingCompleted(context, true)

        if (requestNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onFinish()
    }

    suspend fun goToPage(index: Int) {
        if (animatorsEnabled) {
            pagerState.animateScrollToPage(index)
        } else {
            pagerState.scrollToPage(index)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Branding, Step State & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconShape = RoundedCornerShape(9.dp)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(iconShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF1E1E20), Color(0xFF121212))
                                ),
                                shape = iconShape
                            )
                            .border(
                                width = 0.8.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.08f),
                                shape = iconShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = "PolicyDesk",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.5f)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PolicyDesk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Step ${pagerState.currentPage + 1} of ${pages.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelSecondary,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = { completeOnboarding(requestNotification = false) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentBlue
                        )
                    }
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                val isLastPage = pageIndex == pages.size - 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon Squircle
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .background(
                                color = page.accentColor.copy(alpha = if (isDark) 0.22f else 0.12f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.25.dp,
                                color = page.accentColor.copy(alpha = if (isDark) 0.42f else 0.26f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.accentColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = page.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = labelSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Features Card (LiquidGlassCard already applies 16.dp inner padding)
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 3.dp,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            page.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                color = page.accentColor.copy(alpha = 0.16f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = AppleIcons.Checkmark,
                                            contentDescription = null,
                                            tint = page.accentColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = feature,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = labelPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Optional Setup on final slide
                    if (isLastPage) {
                        Spacer(modifier = Modifier.height(16.dp))

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Quick Advisor Setup (Optional)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary
                                )
                                Text(
                                    text = "Appears automatically on client renewal reminders. Editable anytime in Settings.",
                                    fontSize = 11.5.sp,
                                    color = labelSecondary,
                                    lineHeight = 15.sp
                                )

                                OutlinedTextField(
                                    value = advisorName,
                                    onValueChange = { advisorName = it },
                                    label = { Text("Your Name", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. Rajesh Kumar", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = formFieldColors()
                                )

                                OutlinedTextField(
                                    value = agencyName,
                                    onValueChange = { agencyName = it },
                                    label = { Text("Agency / Firm Name", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. Star Insurance Associates", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = formFieldColors()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Section: Accessible Page Indicator & Action CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 7.dp,
                            animationSpec = if (animatorsEnabled) {
                                spring(stiffness = Spring.StiffnessMediumLow)
                            } else {
                                snap()
                            },
                            label = "indicatorWidth"
                        )
                        val color = if (isSelected) {
                            pages[pagerState.currentPage].accentColor
                        } else {
                            if (isDark) Color(0x40FFFFFF) else Color(0x28000000)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Tab) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch { goToPage(index) }
                                }
                                .semantics {
                                    selected = isSelected
                                    contentDescription = "Slide ${index + 1} of ${pages.size}"
                                }
                                .padding(horizontal = 4.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(7.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CTA Button
                val isLast = pagerState.currentPage == pages.size - 1
                AppleButton(
                    text = if (isLast) "Get Started" else "Continue",
                    onClick = {
                        if (isLast) {
                            completeOnboarding(requestNotification = true)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                goToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    height = 50.dp
                )
            }
        }
    }
}
