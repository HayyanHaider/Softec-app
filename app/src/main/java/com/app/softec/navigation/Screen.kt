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
    data object Settings : Screen

    @Serializable
    data class Details(val id: String) : Screen

    @Serializable
    data object Profile : Screen
}
