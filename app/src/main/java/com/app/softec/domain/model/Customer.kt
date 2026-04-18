package com.app.softec.domain.model

data class Customer (
    val customerId: String,
    val customerName: String,
    val contactNumber: String,
    val email: String? = null
)

