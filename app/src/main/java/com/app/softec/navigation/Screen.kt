package com.app.softec.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Explore : Screen

    @Serializable
    data object Alerts : Screen

    @Serializable
    data class Details(val id: String) : Screen

    @Serializable
    data object Profile : Screen
}
