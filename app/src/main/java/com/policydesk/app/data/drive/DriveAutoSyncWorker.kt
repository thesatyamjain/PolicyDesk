package com.policydesk.app.data.drive

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.policydesk.app.data.local.dao.VehicleDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager job that automatically uploads an encrypted snapshot
 * of the policy database to the user's private Google Drive Vault.
 *
 * Runs silently in the background when the device is connected to the network
 * and battery is healthy, ensuring zero data loss without manual intervention.
 */
class DriveAutoSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AutoSyncWorkerEntryPoint {
        fun vehicleDao(): VehicleDao
    }

    override suspend fun doWork(): Result {
        val isDriveLinked = GoogleDriveManager.isDriveLinked(applicationContext)
        val hasSavedUri = GoogleDriveManager.getSavedBackupUri(applicationContext) != null

        // If neither Drive nor SAF backup is configured, nothing to sync in the background
        if (!isDriveLinked && !hasSavedUri) {
            return Result.success()
        }

        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AutoSyncWorkerEntryPoint::class.java
            )
            val vehicleDao = entryPoint.vehicleDao()
            val entities = vehicleDao.getAllVehicles().first()
            val domainVehicles = entities.map { it.toDomain() }

            val snapshotJson = GoogleDriveManager.createSnapshotJson(domainVehicles)
            val syncResult = GoogleDriveManager.performAutoSync(applicationContext, snapshotJson)

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                val error = syncResult.exceptionOrNull()
                if (error is IOException || error is java.net.SocketTimeoutException) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (e is IOException) Result.retry() else Result.failure()
        }
    }
}

object DriveAutoSyncScheduler {
    private const val PERIODIC_WORK_NAME = "PolicyDeskDriveAutoSyncWork"
    private const val IMMEDIATE_WORK_NAME = "PolicyDeskDriveImmediateSyncWork"

    /**
     * Schedules periodic 12-hour background sync to Google Drive.
     * Enforced only when the device is connected to internet and battery is not low.
     */
    fun scheduleAutoSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<DriveAutoSyncWorker>(
            12, TimeUnit.HOURS,
            2, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Triggers an immediate one-off background sync (e.g. after adding or modifying a policy).
     */
    fun syncImmediately(context: Context) {
        if (!GoogleDriveManager.isDriveLinked(context) && GoogleDriveManager.getSavedBackupUri(context) == null) {
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<DriveAutoSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    /**
     * Cancels the scheduled periodic sync (e.g. when Google Drive is disconnected).
     */
    fun cancelAutoSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }
}
