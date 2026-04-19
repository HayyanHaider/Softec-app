package com.app.softec.core.di

import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.auth.FirebaseAuthRepository
import com.app.softec.data.remote.storage.FirebaseStorageManager
import com.app.softec.data.remote.storage.StorageManager
import com.app.softec.data.repository.DataStoreSettingsRepository
import com.app.softec.data.repository.DefaultSyncItemRepository
import com.app.softec.data.repository.SyncItemRepository
import com.app.softec.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindStorageManager(impl: FirebaseStorageManager): StorageManager

    @Binds
    @Singleton
    abstract fun bindSyncItemRepository(impl: DefaultSyncItemRepository): SyncItemRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}

