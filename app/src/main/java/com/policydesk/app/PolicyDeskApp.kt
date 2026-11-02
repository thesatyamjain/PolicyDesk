package com.policydesk.app

import android.app.Application
import com.policydesk.app.data.drive.DriveAutoSyncScheduler
import com.policydesk.app.notification.NotificationHelper
import com.policydesk.app.notification.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PolicyDeskApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        ReminderScheduler.scheduleDailyReminders(this)
        DriveAutoSyncScheduler.scheduleAutoSync(this)
    }
}
