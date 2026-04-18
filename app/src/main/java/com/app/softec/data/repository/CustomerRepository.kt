package com.app.softec.data.repository

import com.app.softec.data.local.dao.CustomerDao
import com.app.softec.data.mapper.CustomerMappers.toDomain
import com.app.softec.data.mapper.CustomerMappers.toEntity
import com.app.softec.data.mapper.CustomerMappers.toFirestore
import com.app.softec.data.remote.firestore.CustomerFirestore
import com.app.softec.domain.model.Customer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val firestore: FirebaseFirestore
) {
    fun getAllCustomers(userId: String): Flow<List<Customer>> =
        customerDao.getAllCustomers(userId).map { entities -> entities.map { it.toDomain() } }

    fun getCustomerFlow(customerId: String): Flow<Customer?> =
        customerDao.getCustomerFlow(customerId).map { entity -> entity?.toDomain() }

    suspend fun insertCustomer(customer: Customer, userId: String) {
        customerDao.insertCustomer(customer.toEntity(userId))
    }

    suspend fun syncUnsynced(userId: String) {
        val unsynced = customerDao.getUnsyncedCustomers(userId)
        unsynced.forEach { entity ->
            try {
                firestore.collection("users").document(userId)
                    .collection("customers").document(entity.id)
                    .set(entity.toFirestore())
                    .await()
                customerDao.insertCustomer(entity.copy(isSynced = true))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun pullFromFirestore(userId: String) {
        try {
            val docs = firestore.collection("users").document(userId)
                .collection("customers").get().await()
            docs.documents.forEach { doc ->
                val firestoreObj = doc.toObject(CustomerFirestore::class.java)
                firestoreObj?.let {
                    customerDao.insertCustomer(it.toEntity(userId))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}