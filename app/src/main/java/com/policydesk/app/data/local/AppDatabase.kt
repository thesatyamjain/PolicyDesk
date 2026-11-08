package com.policydesk.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.policydesk.app.data.local.dao.VehicleDao
import com.policydesk.app.data.local.entity.PolicyEntity
import com.policydesk.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        PolicyEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao

    companion object {
        const val DATABASE_NAME = "policydesk.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE policies ADD COLUMN commissionPercentage REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE policies ADD COLUMN commissionAmount REAL DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN plateStyle TEXT DEFAULT NULL")
            }
        }
    }
}
