package com.policydesk.app.notification

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager

object NotificationPreferenceManager {
    private const val PREFS_NAME = "policydesk_notifications_prefs"
    private const val KEY_ENABLED = "notifications_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            ReminderScheduler.scheduleDailyReminders(context)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("PolicyReminderWork")
        }
    }
}
