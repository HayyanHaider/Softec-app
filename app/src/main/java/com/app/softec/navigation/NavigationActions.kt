package com.app.softec.navigation

import androidx.navigation.NavController

class NavigationActions(private val navController: NavController) {

    fun navigateTo(screen: Screen) {
        navController.navigate(screen) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToBottomTab(screen: Screen) {
        navController.navigate(screen) {
            popUpTo(Screen.Home) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
