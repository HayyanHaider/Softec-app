package com.app.softec.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object Customers : Screen

    @Serializable
    data object Invoices : Screen

    @Serializable
    data class InvoiceDetail(val id: String) : Screen

    @Serializable
    data class InvoiceFollowUp(val id: String) : Screen

    @Serializable
    data class AddInvoice(val customerId: String) : Screen

    @Serializable
    data class EditInvoice(val id: String) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object ReminderTemplates : Screen

    @Serializable
    data class Details(val id: String) : Screen

    @Serializable
    data object Profile : Screen
}
