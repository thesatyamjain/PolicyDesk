package com.policydesk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.domain.model.ExistingClientInfo
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.theme.AppleDarkSystemDeepOrange
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemOrange
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary

object UiFormatters {
    private val DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val MONTH_YEAR_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatDate(epochMillis: Long): String {
        return java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .format(DATE_FORMATTER)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        return java.time.LocalDate.of(year, month, 1).format(MONTH_YEAR_FORMATTER)
    }

    fun formatMonthYear(epochMillis: Long): String {
        return java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .format(MONTH_YEAR_FORMATTER)
    }

    fun formatTenure(sinceEpochMs: Long, nowEpochMs: Long = System.currentTimeMillis()): String {
        if (sinceEpochMs <= 0L || sinceEpochMs >= nowEpochMs) return "Recent"
        val days = (nowEpochMs - sinceEpochMs) / (24L * 60 * 60 * 1000L)
        val years = days / 365
        val months = (days % 365) / 30
        return when {
            years >= 1 -> {
                if (months > 0) "$years yr, $months mo" else "$years ${if (years == 1L) "year" else "years"}"
            }
            months >= 1 -> "$months ${if (months == 1L) "month" else "months"}"
            days > 0 -> "$days days"
            else -> "Recent"
        }
    }

    fun formatCurrency(amount: Double): String {
        if (amount.isNaN() || amount.isInfinite()) return "₹0"
        val rounded = kotlin.math.round(amount).toLong()
        val sign = if (rounded < 0) "-" else ""
        val absVal = kotlin.math.abs(rounded).toString()
        if (absVal.length <= 3) return "${sign}₹$absVal"
        val last3 = absVal.takeLast(3)
        val remaining = absVal.dropLast(3)
        val formattedRemaining = remaining.reversed().chunked(2).joinToString(",").reversed()
        return "${sign}₹$formattedRemaining,$last3"
    }

    /**
     * Formats positive days (>30) into compound "X months, Y days" or "X years, Y months" for banners.
     */
    fun formatCompoundDurationDetailed(daysRemaining: Long): String {
        if (daysRemaining <= 0) return "${daysRemaining} days"
        if (daysRemaining <= 30) return if (daysRemaining == 1L) "1 day" else "$daysRemaining days"

        val years = daysRemaining / 365
        val remDaysAfterYears = daysRemaining % 365
        val months = remDaysAfterYears / 30
        val days = remDaysAfterYears % 30

        val parts = mutableListOf<String>()
        if (years > 0) {
            parts.add(if (years == 1L) "1 year" else "$years years")
        }
        if (months > 0) {
            parts.add(if (months == 1L) "1 month" else "$months months")
        }
        if (days > 0 && (years == 0L || parts.size < 2)) {
            parts.add(if (days == 1L) "1 day" else "$days days")
        }
        return if (parts.isEmpty()) "1 month" else parts.joinToString(", ")
    }

    /**
     * Formats positive days (>30) into compact "Xm Yd" or "Xy Ym" for list badges.
     */
    fun formatCompoundDurationCompact(daysRemaining: Long): String {
        if (daysRemaining <= 0) return "${daysRemaining}d"
        if (daysRemaining <= 30) return "${daysRemaining}d"

        val years = daysRemaining / 365
        val remDaysAfterYears = daysRemaining % 365
        val months = remDaysAfterYears / 30
        val days = remDaysAfterYears % 30

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("${years}y")
        if (months > 0) parts.add("${months}m")
        if (days > 0 && (years == 0L || parts.size < 2)) parts.add("${days}d")

        return if (parts.isEmpty()) "1m" else parts.joinToString(" ")
    }
}

/**
 * Apple Liquid Glass Status Capsule
 * Features translucent frosted tint, subtle specular border, and high-contrast typography.
 * Supports both light mode and dark mode.
 */
@Composable
fun DaysRemainingBadge(
    daysRemaining: Long,
    status: PolicyStatus,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val green = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val orange = if (isDark) AppleDarkSystemOrange else AppleSystemOrange
    val deepOrange = if (isDark) AppleDarkSystemDeepOrange else AppleSystemDeepOrange
    val red = if (isDark) AppleDarkSystemRed else AppleSystemRed
    val gray = if (isDark) Color(0xFFA1A1A6) else Color(0xFF8E8E93)

    val (accentColor, text) = when {
        status == PolicyStatus.CLOSED -> Pair(gray, "Closed")
        status == PolicyStatus.RENEWED -> Pair(green, "Renewed")
        daysRemaining < 0 -> Pair(
            red,
            "Overdue by ${-daysRemaining}d"
        )
        daysRemaining == 0L -> Pair(
            red,
            "Expires Today"
        )
        daysRemaining <= 7 -> Pair(
            deepOrange,
            "Expires in ${daysRemaining}d"
        )
        daysRemaining <= 30 -> Pair(
            orange,
            "${daysRemaining} days left"
        )
        else -> Pair(
            green,
            "${UiFormatters.formatCompoundDurationCompact(daysRemaining)} left"
        )
    }

    val badgeShape = RoundedCornerShape(8.dp)
    val bgAlpha = if (isDark) 0.20f else 0.12f
    val borderAlpha = if (isDark) 0.40f else 0.28f

    Box(
        modifier = modifier
            .background(accentColor.copy(alpha = bgAlpha), badgeShape)
            .border(0.75.dp, accentColor.copy(alpha = borderAlpha), badgeShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

/**
 * VisualTransformation that formats raw Indian vehicle registration numbers (e.g. "MH77HH7777")
 * into standard spaced HSRP format ("MH 77 HH 7777") with bidirectional cursor mapping.
 */
object RegistrationPlateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val breaks = RegistrationNormalizer.getSpaceBreakIndices(original)
        if (breaks.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val origLen = original.length
        val spaceCount = breaks.count { it in 1 until origLen }
        val transLen = origLen + spaceCount

        val origToTrans = IntArray(origLen + 1)
        val transToOrig = IntArray(transLen + 1)

        val sb = StringBuilder(transLen)
        var transIdx = 0

        for (origIdx in 0 until origLen) {
            if (origIdx in breaks) {
                sb.append(' ')
                transToOrig[transIdx] = origIdx
                transIdx++
            }
            origToTrans[origIdx] = transIdx
            transToOrig[transIdx] = origIdx
            sb.append(original[origIdx])
            transIdx++
        }
        origToTrans[origLen] = transIdx
        transToOrig[transIdx] = origLen

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, origToTrans.size - 1)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToOrig[offset.coerceIn(0, transToOrig.size - 1)]
            }
        }

        return TransformedText(AnnotatedString(sb.toString()), offsetMapping)
    }
}

/**
 * Apple HIG / Liquid Glass banner displayed when an existing client and renewal policy
 * are detected from vehicle registration number (either during manual entry or AI scan).
 */
@Composable
fun ExistingClientRenewalBanner(
    existingInfo: ExistingClientInfo,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentColor = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val cardBg = if (isDark) Color(0xFF0F2644) else Color(0xFFF0F7FF)
    val borderColor = if (isDark) Color(0xFF1D4ED8).copy(alpha = 0.5f) else Color(0xFF93C5FD)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.History,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Existing Client & Renewal Detected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        val tenureExtra = existingInfo.clientSinceEpochMs?.let {
                            " • Client for ${UiFormatters.formatTenure(it)}"
                        } ?: ""
                        Text(
                            text = "Auto-linked to records$tenureExtra",
                            fontSize = 11.sp,
                            color = labelSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (existingInfo.totalPoliciesCount > 1) "Policy #${existingInfo.totalPoliciesCount + 1}" else "Renewal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            // Client row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = AppleIcons.Person,
                    contentDescription = null,
                    tint = labelSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${existingInfo.clientName} (${existingInfo.mobile})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelPrimary
                )
            }

            // Previous policy row
            if (!existingInfo.previousPolicyNumber.isNullOrBlank()) {
                val expiryText = existingInfo.previousExpiryEpochMs?.let {
                    " • Expired: ${UiFormatters.formatDate(it)}"
                } ?: ""
                Text(
                    text = "Previous: ${existingInfo.previousInsurer ?: "Insurer"} [${existingInfo.previousPolicyNumber}]$expiryText",
                    fontSize = 12.sp,
                    color = labelSecondary,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Text(
                text = "Contact info and prior commission % have been auto-filled from records.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = labelSecondary.copy(alpha = 0.9f),
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
