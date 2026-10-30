package com.policydesk.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.policydesk.app.data.local.AppDatabase
import com.policydesk.app.data.local.dao.VehicleDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class PolicyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderWorkerEntryPoint {
        fun vehicleDao(): VehicleDao
    }

    override suspend fun doWork(): Result {
        if (!NotificationPreferenceManager.areNotificationsEnabled(applicationContext)) {
            return Result.success()
        }

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ReminderWorkerEntryPoint::class.java
        )
        val vehicleDao = entryPoint.vehicleDao()
        val allEntities = vehicleDao.getAllVehicles().first()
        val todayEpochDay = LocalDate.now().toEpochDay()

        var todayExpiring = 0
        var thisWeekExpiring = 0
        var overdue = 0

        val targetDays = setOf(30L, 15L, 7L, 1L, 0L)

        for ((index, entity) in allEntities.withIndex()) {
            val domain = entity.toDomain()
            if (domain.vehicle.isClosed) continue
            val policy = domain.currentPolicy ?: continue

            val days = policy.computeDaysRemaining(todayEpochDay)

            when {
                days < 0 -> overdue++
                days == 0L -> {
                    todayExpiring++
                    thisWeekExpiring++
                }
                days in 1..7 -> thisWeekExpiring++
            }

            if (targetDays.contains(days)) {
                NotificationHelper.showPolicyExpiryAlert(
                    context = applicationContext,
                    regNo = domain.vehicle.regNo,
                    customerName = domain.vehicle.customer.name,
                    mobile = domain.vehicle.customer.mobile,
                    insurer = policy.insurer,
                    daysRemaining = days,
                    notificationId = 1000 + index
                )
            }
        }

        // Daily Digest Alert
        NotificationHelper.showDailyDigest(
            context = applicationContext,
            todayCount = todayExpiring,
            thisWeekCount = thisWeekExpiring,
            overdueCount = overdue
        )

        // Silent Background Drive Backup (PRD §5.9)
        if (com.policydesk.app.data.drive.GoogleDriveManager.isDriveLinked(applicationContext) ||
            com.policydesk.app.data.drive.GoogleDriveManager.getSavedBackupUri(applicationContext) != null) {
            try {
                val vehicles = allEntities.map { it.toDomain() }
                val snapshot = com.policydesk.app.data.drive.GoogleDriveManager.createSnapshotJson(vehicles)
                com.policydesk.app.data.drive.GoogleDriveManager.performSilentBackup(applicationContext, snapshot)
            } catch (_: Exception) {
                // Local-first, fails gracefully without disrupting reminders
            }
        }

        return Result.success()
    }
}

object ReminderScheduler {
    private const val WORK_NAME = "PolicyReminderWork"

    fun scheduleDailyReminders(context: Context) {
        val request = PeriodicWorkRequestBuilder<PolicyReminderWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
