package com.app.softec.data.mapper

import com.app.softec.data.local.entity.CustomerEntity
import com.app.softec.data.remote.firestore.CustomerFirestore
import com.app.softec.domain.model.Customer

object CustomerMappers {
    fun Customer.toEntity(userId: String, isSynced: Boolean = false): CustomerEntity =
        CustomerEntity(id = customerId, name = customerName, contactNumber = contactNumber, email = email, userId = userId, isSynced = isSynced)

    fun CustomerEntity.toDomain(): Customer =
        Customer(customerId = id, customerName = name, contactNumber = contactNumber, email = email)

    fun CustomerEntity.toFirestore(): CustomerFirestore =
        CustomerFirestore(id = id, name = name, contactNumber = contactNumber, email = email)
    
    fun CustomerFirestore.toEntity(userId: String): CustomerEntity =
        CustomerEntity(id = id, name = name, contactNumber = contactNumber, email = email, userId = userId, isSynced = true)
}