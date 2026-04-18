package com.app.softec.data.repository

import com.app.softec.data.local.dao.PaymentHistoryDao
import com.app.softec.data.local.entity.PaymentHistoryEntity
import com.app.softec.data.mapper.PaymentHistoryMappers.toEntity
import com.app.softec.data.mapper.PaymentHistoryMappers.toDomain
import com.app.softec.domain.model.PaymentHistory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentHistoryDao,
    private val firestore: FirebaseFirestore
) {
    
    // Get payments by account
    fun getPaymentsByAccount(accountId: String): Flow<List<PaymentHistory>> =
        paymentDao.getPaymentsByAccount(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get all payments for user
    fun getAllPayments(userId: String): Flow<List<PaymentHistory>> =
        paymentDao.getAllPayments(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    
    // Get total payments for account
    fun getTotalPaymentsByAccount(accountId: String): Flow<Double> =
        paymentDao.getTotalPaymentsByAccount(accountId)
    
    // Get single payment
    suspend fun getPaymentById(paymentId: String): PaymentHistory? =
        paymentDao.getPaymentById(paymentId)?.toDomain()
    
    // Insert payment
    suspend fun insertPayment(payment: PaymentHistory, userId: String) {
        val entity = payment.toEntity(userId)
        paymentDao.insertPayment(entity)
    }
    
    // Delete payment
    suspend fun deletePayment(payment: PaymentHistory) {
        paymentDao.deletePayment(payment.toEntity(""))
    }
    
    // Sync unsynced payments to Firestore
    suspend fun syncUnsynced(userId: String) {
        val unsyncedPayments = paymentDao.getUnsyncedPayments(userId)
        
        unsyncedPayments.forEach { entity ->
            try {
                firestore.collection("users").document(userId)
                    .collection("payments").document(entity.id)
                    .set(entity.toEntity(""))
                    .await()
                
                // Mark as synced
                paymentDao.insertPayment(entity.copy(isSynced = true))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Pull payments from Firestore
    suspend fun pullFromFirestore(userId: String) {
        try {
            val documents = firestore.collection("users").document(userId)
                .collection("payments").get().await()
            
            documents.documents.forEach { doc ->
                val entity = doc.toObject(PaymentHistoryEntity::class.java)?.copy(
                    isSynced = true,
                    userId = userId
                )
                if (entity != null) {
                    paymentDao.insertPayment(entity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

