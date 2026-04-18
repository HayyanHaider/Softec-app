package com.app.softec.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.app.softec.data.local.entity.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentHistoryDao {
    @Insert
    suspend fun insertPayment(payment: PaymentHistoryEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentHistoryEntity)

    @Query("SELECT * FROM payment_history WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: String): PaymentHistoryEntity?

    @Query("SELECT * FROM payment_history WHERE accountId = :accountId ORDER BY paymentDate DESC")
    fun getPaymentsByAccount(accountId: String): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history WHERE userId = :userId ORDER BY paymentDate DESC")
    fun getAllPayments(userId: String): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT SUM(amount) FROM payment_history WHERE accountId = :accountId")
    fun getTotalPaymentsByAccount(accountId: String): Flow<Double>

    @Query("SELECT * FROM payment_history WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedPayments(userId: String): List<PaymentHistoryEntity>

    @Query("DELETE FROM payment_history WHERE userId = :userId")
    suspend fun deleteAllPaymentsByUser(userId: String)
}

