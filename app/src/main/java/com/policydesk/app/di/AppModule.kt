package com.policydesk.app.di

import android.content.Context
import androidx.room.Room
import com.policydesk.app.data.local.AppDatabase
import com.policydesk.app.data.local.dao.VehicleDao
import com.policydesk.app.data.repository.VehicleRepositoryImpl
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: AppDatabase): VehicleDao {
        return database.vehicleDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository
}
