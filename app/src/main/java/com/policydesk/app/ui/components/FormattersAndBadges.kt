package com.policydesk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.theme.AppleDarkSystemDeepOrange
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemOrange
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemRed

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
            "${daysRemaining} days left"
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
