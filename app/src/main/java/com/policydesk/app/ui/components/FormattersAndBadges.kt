package com.policydesk.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object UiFormatters {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DATE_FORMATTER)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        return LocalDate.of(year, month, 1).format(MONTH_YEAR_FORMATTER)
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}

/**
 * Apple Liquid Glass Status Capsule
 * Features translucent frosted tint, subtle specular border, and high-contrast typography.
 */
@Composable
fun DaysRemainingBadge(
    daysRemaining: Long,
    status: PolicyStatus,
    modifier: Modifier = Modifier
) {
    val (accentColor, text) = when {
        status == PolicyStatus.CLOSED -> Pair(Color(0xFF8E8E93), "Closed")
        status == PolicyStatus.RENEWED -> Pair(AppleSystemGreen, "Renewed")
        daysRemaining < 0 -> Pair(
            AppleSystemRed,
            "Overdue by ${-daysRemaining}d"
        )
        daysRemaining == 0L -> Pair(
            AppleSystemRed,
            "Expires Today"
        )
        daysRemaining <= 7 -> Pair(
            AppleSystemDeepOrange,
            "Expires in ${daysRemaining}d"
        )
        daysRemaining <= 30 -> Pair(
            AppleSystemOrange,
            "${daysRemaining} days left"
        )
        else -> Pair(
            AppleSystemGreen,
            "${daysRemaining} days left"
        )
    }

    val badgeShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .background(accentColor.copy(alpha = 0.12f), badgeShape)
            .border(0.75.dp, accentColor.copy(alpha = 0.28f), badgeShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp
        )
    }
}
