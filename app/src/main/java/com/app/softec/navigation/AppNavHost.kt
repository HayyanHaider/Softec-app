package com.app.softec.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.screens.home.HomeDataScreen
import com.app.softec.ui.screens.splash.HackathonSplashScreen

private data class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        screen = Screen.Home,
        label = "Home",
        icon = Icons.Filled.Home
    ),
    TopLevelDestination(
        screen = Screen.Explore,
        label = "Explore",
        icon = Icons.Filled.Explore
    ),
    TopLevelDestination(
        screen = Screen.Alerts,
        label = "Alerts",
        icon = Icons.Filled.Notifications
    ),
    TopLevelDestination(
        screen = Screen.Profile,
        label = "Profile",
        icon = Icons.Filled.Person
    )
)

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Splash
) {
    val navActions = remember(navController) { NavigationActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Screen.Splash> {
            HackathonSplashScreen(
                onAnimationComplete = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Screen.Home> {
            TopLevelScreen(
                title = "Home",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                HomeDataScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenDetails = { itemId -> navActions.navigateTo(Screen.Details(itemId)) }
                )
            }
        }
        composable<Screen.Explore> {
            TopLevelScreen(
                title = "Explore",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                TabPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    message = "Explore ideas, tasks, and experiments."
                )
            }
        }
        composable<Screen.Alerts> {
            TopLevelScreen(
                title = "Alerts",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                TabPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    message = "View reminders and hackathon updates."
                )
            }
        }
        composable<Screen.Profile> {
            TopLevelScreen(
                title = "Profile",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                TabPlaceholder(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    message = "Manage your team and preferences."
                )
            }
        }
        composable<Screen.Details> { backStackEntry ->
            val details: Screen.Details = backStackEntry.toRoute()
            StandardScaffold(
                title = "Details",
                showBackButton = true,
                onNavigateBack = navActions::navigateBack
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Details ID: ${details.id}")
                }
            }
        }
    }
}

@Composable
private fun TopLevelScreen(
    title: String,
    currentDestination: NavDestination?,
    onSelectTab: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    StandardScaffold(
        title = title,
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination.isTopLevelDestinationSelected(destination.screen),
                        onClick = { onSelectTab(destination.screen) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun TabPlaceholder(
    modifier: Modifier = Modifier,
    message: String
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = message)
    }
}

private fun NavDestination?.isTopLevelDestinationSelected(screen: Screen): Boolean {
    return when (screen) {
        Screen.Home -> this?.hierarchy?.any { it.hasRoute<Screen.Home>() } == true
        Screen.Explore -> this?.hierarchy?.any { it.hasRoute<Screen.Explore>() } == true
        Screen.Alerts -> this?.hierarchy?.any { it.hasRoute<Screen.Alerts>() } == true
        Screen.Profile -> this?.hierarchy?.any { it.hasRoute<Screen.Profile>() } == true
        else -> false
    }
}
