package com.policydesk.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.policydesk.app.data.local.dao.VehicleDao
import com.policydesk.app.data.local.entity.PolicyEntity
import com.policydesk.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        PolicyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao

    companion object {
        const val DATABASE_NAME = "policydesk.db"
    }
}
