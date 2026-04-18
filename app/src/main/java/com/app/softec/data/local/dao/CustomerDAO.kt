package com.app.softec.data.local.dao

import androidx.room.*
import com.app.softec.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE userId = :userId")
    fun getAllCustomers(userId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerFlow(id: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedCustomers(userId: String): List<CustomerEntity>

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}