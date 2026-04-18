package com.app.softec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.softec.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AccountDao {
    @Insert
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    fun getAllAccountsByUser(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE customerId = :customerId")
    fun getAccountsByCustomerId(customerId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND status = 'overdue'")
    fun getOverdueAccounts(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND dueDate <= :today AND status != 'paid'")
    fun getDueAccountsToday(userId: String, today: Date): Flow<List<AccountEntity>>

    @Query("SELECT SUM(amountRemaining) FROM accounts WHERE userId = :userId AND status != 'paid'")
    fun getTotalPendingAmount(userId: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM accounts WHERE userId = :userId AND nextFollowUpDate <= :today AND nextFollowUpDate IS NOT NULL")
    fun getFollowUpsNeededToday(userId: String, today: Date): Flow<Int>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND status = 'overdue' ORDER BY dueDate ASC")
    fun getOverdueAccountsOrdered(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedAccounts(userId: String): List<AccountEntity>

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAllAccountsByUser(userId: String)
}

