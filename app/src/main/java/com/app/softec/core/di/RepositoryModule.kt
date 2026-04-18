package com.app.softec.core.di

import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.auth.FirebaseAuthRepository
import com.app.softec.data.remote.storage.FirebaseStorageManager
import com.app.softec.data.remote.storage.StorageManager
import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.DefaultSyncItemRepository
import com.app.softec.data.repository.FollowUpRepository
import com.app.softec.data.repository.PaymentRepository
import com.app.softec.data.repository.SyncItemRepository
import com.app.softec.data.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
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
}

@Module
@InstallIn(SingletonComponent::class)
object DataRepositoryModule {
    
    @Singleton
    @Provides
    fun provideAccountRepository(impl: AccountRepository): AccountRepository = impl
    
    @Singleton
    @Provides
    fun provideFollowUpRepository(impl: FollowUpRepository): FollowUpRepository = impl
    
    @Singleton
    @Provides
    fun providePaymentRepository(impl: PaymentRepository): PaymentRepository = impl
    
    @Singleton
    @Provides
    fun provideSyncRepository(impl: SyncRepository): SyncRepository = impl
}

