package com.app.softec.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val contactNumber: String,
    val email: String?,
    val userId: String,
    val isSynced: Boolean = false
)