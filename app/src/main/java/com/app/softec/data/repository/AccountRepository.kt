package com.app.softec.data.repository

import com.app.softec.data.local.dao.AccountDao
import com.app.softec.data.local.entity.AccountEntity
import com.app.softec.data.mapper.AccountMappers.toEntity
import com.app.softec.data.mapper.AccountMappers.toDomain
import com.app.softec.data.mapper.AccountMappers.toFirestore
import com.app.softec.domain.model.Account
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val firestore: FirebaseFirestore
) {
    
    // Get all accounts for user
    fun getAllAccounts(userId: String): Flow<List<Account>> =
        accountDao.getAllAccountsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get overdue accounts
    fun getOverdueAccounts(userId: String): Flow<List<Account>> =
        accountDao.getOverdueAccounts(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get accounts due today
    fun getDueAccountsToday(userId: String, today: java.util.Date): Flow<List<Account>> =
        accountDao.getDueAccountsToday(userId, today).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get total pending amount
    fun getTotalPendingAmount(userId: String): Flow<Double> =
        accountDao.getTotalPendingAmount(userId)
    
    // Get follow-ups needed today
    fun getFollowUpsNeededToday(userId: String, today: java.util.Date): Flow<Int> =
        accountDao.getFollowUpsNeededToday(userId, today)
    
    // Get single account
    suspend fun getAccountById(accountId: String): Account? =
        accountDao.getAccountById(accountId)?.toDomain()
    
    // Insert account locally
    suspend fun insertAccount(account: Account, userId: String) {
        val entity = account.toEntity(userId)
        accountDao.insertAccount(entity)
    }
    
    // Update account
    suspend fun updateAccount(account: Account, userId: String) {
        val entity = account.toEntity(userId)
        accountDao.updateAccount(entity)
    }
    
    // Delete account
    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account.toEntity(""))
    }
    
    // Sync unsynced accounts to Firestore
    suspend fun syncUnsynced(userId: String) {
        val unsyncedAccounts = accountDao.getUnsyncedAccounts(userId)
        
        unsyncedAccounts.forEach { entity ->
            try {
                firestore.collection("users").document(userId)
                    .collection("accounts").document(entity.id)
                    .set(entity.toFirestore())
                    .await()
                
                // Mark as synced
                accountDao.updateAccount(entity.copy(isSynced = true))
            } catch (e: Exception) {
                // Log error, retry later
                e.printStackTrace()
            }
        }
    }
    
    // Pull accounts from Firestore
    suspend fun pullFromFirestore(userId: String) {
        try {
            val documents = firestore.collection("users").document(userId)
                .collection("accounts").get().await()
            
            documents.documents.forEach { doc ->
                val entity = doc.toObject(AccountEntity::class.java)?.copy(
                    isSynced = true,
                    userId = userId
                )
                if (entity != null) {
                    accountDao.insertAccount(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

