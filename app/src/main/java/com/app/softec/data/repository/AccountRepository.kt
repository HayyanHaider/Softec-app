package com.app.softec.data.repository

import com.app.softec.data.local.dao.AccountDao
import com.app.softec.data.local.entity.AccountEntity
import com.app.softec.data.mapper.AccountMappers.toEntity
import com.app.softec.data.mapper.AccountMappers.toDomain
import com.app.softec.data.mapper.AccountMappers.toFirestore
import com.app.softec.data.remote.firestore.AccountFirestore
import com.app.softec.data.remote.firestore.snapshotAsFlow
import com.app.softec.domain.model.Account
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val firestore: FirebaseFirestore
) {

    private var realtimeSyncJob: Job? = null
    private var realtimeSyncUserId: String? = null
    
    // Get all accounts for user
    fun getAllAccounts(userId: String): Flow<List<Account>> =
        accountDao.getAllAccountsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getAccountsByCustomerId(customerId: String): Flow<List<Account>> =
        accountDao.getAccountsByCustomerId(customerId).map { entities ->
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
        syncUnsynced(userId)
    }

    fun bindRealtimeSync(userId: String, scope: CoroutineScope) {
        if (realtimeSyncJob?.isActive == true && realtimeSyncUserId == userId) return

        realtimeSyncJob?.cancel()
        realtimeSyncUserId = userId

        realtimeSyncJob = scope.launch(Dispatchers.IO) {
            firestore.collection("users").document(userId)
                .collection("accounts")
                .snapshotAsFlow<AccountFirestore>()
                .catch { throwable ->
                    if (throwable is FirebaseFirestoreException && throwable.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        return@catch
                    }
                    throw throwable
                }
                .collectLatest { remoteAccounts ->
                    remoteAccounts.forEach { remote ->
                        mergeRemoteAccount(remote.toEntity(userId))
                    }
                }
        }
    }
    
    // Update account
    suspend fun updateAccount(account: Account, userId: String) {
        val entity = account.toEntity(userId)
        accountDao.updateAccount(entity)
        syncUnsynced(userId)
    }
    
    // Delete account
    suspend fun deleteAccount(account: Account, userId: String) {
        accountDao.deleteAccount(account.toEntity(userId))
        firestore.collection("users").document(userId)
            .collection("accounts").document(account.id)
            .delete()
            .await()
    }
    
    // Sync unsynced accounts to Firestore
    suspend fun syncUnsynced(userId: String) {
        val unsyncedAccounts = accountDao.getUnsyncedAccounts(userId)
        
        unsyncedAccounts.forEach { entity ->
            firestore.collection("users").document(userId)
                .collection("accounts").document(entity.id)
                .set(entity.toFirestore())
                .await()

            // Mark as synced
            accountDao.updateAccount(entity.copy(isSynced = true))
        }
    }
    
    // Pull accounts from Firestore
    suspend fun pullFromFirestore(userId: String) {
        val documents = firestore.collection("users").document(userId)
            .collection("accounts").get().await()

        documents.documents.forEach { doc ->
            val remote = doc.toObject(AccountFirestore::class.java)
            if (remote != null) {
                mergeRemoteAccount(remote.toEntity(userId))
            }
        }
    }

    private suspend fun mergeRemoteAccount(remoteEntity: AccountEntity) {
        val localEntity = accountDao.getAccountById(remoteEntity.id)

        val shouldApplyRemote = when {
            localEntity == null -> true
            localEntity.isSynced -> true
            localEntity.updatedAt.before(remoteEntity.updatedAt) -> true
            else -> false
        }

        if (shouldApplyRemote) {
            accountDao.insertAccount(remoteEntity.copy(isSynced = true))
        }
    }
}

