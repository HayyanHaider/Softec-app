package com.app.softec.core.di

import android.content.Context
import androidx.room.Room
import com.app.softec.data.local.AppDatabase
import com.app.softec.data.local.dao.SyncItemDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSyncItemDao(database: AppDatabase): SyncItemDao {
        return database.syncItemDao()
    }

    private const val DATABASE_NAME = "softec.db"
}
