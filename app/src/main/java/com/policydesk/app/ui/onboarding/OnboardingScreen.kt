package com.policydesk.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.data.onboarding.OnboardingManager
import com.policydesk.app.data.profile.AdvisorProfile
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.SapphireBlue
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
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val labelTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Smart Vehicle Portfolio",
                subtitle = "Manage your entire client vehicle book with live expiry countdowns, status badges, and zero paper chaos.",
                icon = AppleIcons.Car,
                accentColor = Color(0xFF007AFF),
                features = listOf(
                    "Real-time expiry countdowns (Active, Expiring, Overdue)",
                    "Realistic HSRP number plates (White, Yellow, EV Green)",
                    "Lightning-fast search by RC, customer name or phone"
                )
            ),
            OnboardingPageData(
                title = "Zero-Effort PDF Intake",
                subtitle = "Share or open any insurer policy PDF into PolicyDesk. Intelligent extraction fills policy & vehicle data automatically.",
                icon = AppleIcons.Brain,
                accentColor = Color(0xFF5856D6),
                features = listOf(
                    "Automatic bilingual & OCR label cleaning",
                    "Customer names automatically formatted to Title Case",
                    "Bulk Excel & CSV spreadsheet portfolio import"
                )
            ),
            OnboardingPageData(
                title = "Instant Client Renewals",
                subtitle = "Send personalized renewal notices on WhatsApp before policies lapse with your branded advisor signature.",
                icon = AppleIcons.WhatsApp,
                accentColor = Color(0xFF34C759),
                features = listOf(
                    "One-tap pre-filled WhatsApp renewal messages",
                    "Customizable message template with your contact details",
                    "Offline-first with secure Google Drive auto-sync"
                )
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

    // Optional quick profile setup on slide 3
    val existingProfile = remember { AdvisorProfileManager.getProfile(context) }
    var advisorName by remember { mutableStateOf(existingProfile.advisorName) }
    var agencyName by remember { mutableStateOf(existingProfile.agencyName) }

    fun completeOnboarding() {
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
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Branding & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(SapphireBlue, Color(0xFF5856D6))),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.ShieldCheck,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PolicyDesk",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        letterSpacing = (-0.3).sp
                    )
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = { completeOnboarding() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SapphireBlue
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
                    // Glowing Icon Squircle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                color = page.accentColor.copy(alpha = if (isDark) 0.22f else 0.12f),
                                shape = RoundedCornerShape(26.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = page.accentColor.copy(alpha = if (isDark) 0.45f else 0.25f),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.accentColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Features Card
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 3.dp,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                                color = page.accentColor.copy(alpha = 0.18f),
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
                        Spacer(modifier = Modifier.height(18.dp))

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Quick Advisor Setup (Optional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary
                                )
                                Text(
                                    text = "Appears automatically on client renewal reminders. You can edit this anytime in Settings.",
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
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SapphireBlue,
                                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                                        focusedLabelColor = SapphireBlue,
                                        unfocusedLabelColor = labelTertiary
                                    )
                                )

                                OutlinedTextField(
                                    value = agencyName,
                                    onValueChange = { agencyName = it },
                                    label = { Text("Agency / Firm Name", fontSize = 12.sp) },
                                    placeholder = { Text("e.g. Star Insurance Associates", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SapphireBlue,
                                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                                        focusedLabelColor = SapphireBlue,
                                        unfocusedLabelColor = labelTertiary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Section: Page Indicator & Action CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pill Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 7.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "indicatorWidth"
                        )
                        val color = if (isSelected) {
                            pages[pagerState.currentPage].accentColor
                        } else {
                            if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
                        }

                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CTA Button
                val isLast = pagerState.currentPage == pages.size - 1
                AppleButton(
                    text = if (isLast) "Get Started" else "Continue",
                    onClick = {
                        if (isLast) {
                            completeOnboarding()
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
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
