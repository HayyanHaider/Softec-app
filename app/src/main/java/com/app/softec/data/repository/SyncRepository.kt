package com.app.softec.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val accountRepository: AccountRepository,
    private val followUpRepository: FollowUpRepository,
    private val paymentRepository: PaymentRepository
) {
    
    // Sync all unsynced data to Firestore
    suspend fun syncAllUnsynced(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Sync accounts
            accountRepository.syncUnsynced(userId)
            
            // Sync follow-ups
            followUpRepository.syncUnsynced(userId)
            
            // Sync payments
            paymentRepository.syncUnsynced(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Pull all data from Firestore
    suspend fun pullAllFromFirestore(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Pull accounts
            accountRepository.pullFromFirestore(userId)
            
            // Pull follow-ups
            followUpRepository.pullFromFirestore(userId)
            
            // Pull payments
            paymentRepository.pullFromFirestore(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Full sync: Pull then Push
    suspend fun fullSync(userId: String) = withContext(Dispatchers.IO) {
        try {
            // First pull from cloud
            pullAllFromFirestore(userId).getOrThrow()
            
            // Then push local changes
            syncAllUnsynced(userId).getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

