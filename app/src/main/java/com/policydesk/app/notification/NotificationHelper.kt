package com.policydesk.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.policydesk.app.MainActivity
import com.policydesk.app.domain.model.RegistrationNormalizer

object NotificationHelper {
    const val CHANNEL_EXPIRY = "channel_policy_expiry"
    const val CHANNEL_DIGEST = "channel_daily_digest"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val expiryChannel = NotificationChannel(
                CHANNEL_EXPIRY,
                "Policy Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for policies expiring soon or overdue"
                enableVibration(true)
            }

            val digestChannel = NotificationChannel(
                CHANNEL_DIGEST,
                "Daily Expiry Digest",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily 9:00 AM summary of expiring and overdue policies"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(expiryChannel)
            manager.createNotificationChannel(digestChannel)
        }
    }

    fun showPolicyExpiryAlert(
        context: Context,
        regNo: String,
        customerName: String,
        mobile: String,
        insurer: String,
        daysRemaining: Long,
        notificationId: Int
    ) {
        createNotificationChannels(context)

        val title = when {
            daysRemaining < 0 -> "Overdue Alert: $regNo"
            daysRemaining == 0L -> "Expires Today: $regNo"
            daysRemaining <= 7 -> "Urgent: $regNo expires in $daysRemaining days!"
            else -> "Reminder: $regNo expires in $daysRemaining days"
        }

        val content = "$customerName • $insurer (Tap to open policy)"

        // Main Tap Intent -> Open app to Vehicle Detail
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_reg_no", regNo)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Call Customer
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile"))
        val callPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1000,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: WhatsApp
        val formattedMobile = if (mobile.startsWith("91")) mobile else "91$mobile"
        val msg = Uri.encode("Hello $customerName, your vehicle policy for ${RegistrationNormalizer.formatForDisplay(regNo)} is due for renewal.")
        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedMobile?text=$msg"))
        val waPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 2000,
            waIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_EXPIRY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.stat_sys_phone_call, "Call", callPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "WhatsApp", waPendingIntent)

        val manager = NotificationManagerCompat.from(context)
        try {
            manager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun showDailyDigest(
        context: Context,
        todayCount: Int,
        thisWeekCount: Int,
        overdueCount: Int
    ) {
        createNotificationChannels(context)

        val title = "Daily Expiry Summary"
        val content = "Today: $todayCount | This Week: $thisWeekCount | Overdue: $overdueCount"

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DIGEST)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        try {
            manager.notify(9999, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
