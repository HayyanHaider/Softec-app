package com.app.softec.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.screens.auth.AuthScreen
import com.app.softec.ui.screens.auth.SessionState
import com.app.softec.ui.screens.auth.SessionViewModel
import com.app.softec.ui.screens.home.HomeDataScreen
import com.app.softec.ui.screens.splash.HackathonSplashScreen
import com.app.softec.ui.theme.spacing

private data class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        screen = Screen.Customers,
        label = "Customers",
        icon = Icons.Filled.Person
    ),
    TopLevelDestination(
        screen = Screen.Invoices,
        label = "Invoice",
        icon = Icons.Filled.Inventory
    ),
    TopLevelDestination(
        screen = Screen.Settings,
        label = "Settings",
        icon = Icons.Filled.Settings
    )
)

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Splash
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val navActions = remember(navController) { NavigationActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var splashNavigationHandled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(splashFinished, sessionState, splashNavigationHandled) {
        if (!splashFinished || splashNavigationHandled) {
            return@LaunchedEffect
        }

        val destination = when (sessionState) {
            SessionState.Authenticated -> Screen.Customers
            SessionState.Unauthenticated -> Screen.Auth
            SessionState.Checking -> null
        }

        if (destination != null) {
            splashNavigationHandled = true
            navController.navigate(destination) {
                popUpTo(Screen.Splash) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(sessionState, splashNavigationHandled, currentDestination) {
        if (!splashNavigationHandled) {
            return@LaunchedEffect
        }

        when (sessionState) {
            SessionState.Authenticated -> {
                if (currentDestination.isAuthDestination()) {
                    navController.navigate(Screen.Customers) {
                        popUpTo(Screen.Auth) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            SessionState.Unauthenticated -> {
                if (currentDestination.isProtectedDestination()) {
                    navController.navigate(Screen.Auth) {
                        popUpTo(navController.graph.id)
                        launchSingleTop = true
                    }
                }
            }

            SessionState.Checking -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Screen.Splash> {
            HackathonSplashScreen(
                onAnimationComplete = {
                    splashFinished = true
                }
            )
        }
        composable<Screen.Auth> {
            StandardScaffold(title = "Sign in") { innerPadding ->
                AuthScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
        composable<Screen.Customers> {
            TopLevelScreen(
                title = "Customers",
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
        composable<Screen.Invoices> {
            TopLevelScreen(
                title = "Invoices",
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
        composable<Screen.Settings> {
            TopLevelScreen(
                title = "Settings",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                SettingsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onSignOut = sessionViewModel::signOut
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

@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        Text(
            text = "You are signed in.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Use this to end your session on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PrimaryButton(
            text = "Sign out",
            onClick = onSignOut
        )
    }
}

private fun NavDestination?.isTopLevelDestinationSelected(screen: Screen): Boolean {
    return when (screen) {
        Screen.Customers -> this?.hierarchy?.any { it.hasRoute<Screen.Customers>() } == true
        Screen.Invoices -> this?.hierarchy?.any { it.hasRoute<Screen.Invoices>() } == true
        Screen.Settings -> this?.hierarchy?.any { it.hasRoute<Screen.Settings>() } == true
        Screen.Profile -> this?.hierarchy?.any { it.hasRoute<Screen.Profile>() } == true
        else -> false
    }
}

private fun NavDestination?.isAuthDestination(): Boolean {
    return this?.hierarchy?.any { it.hasRoute<Screen.Auth>() } == true
}

private fun NavDestination?.isProtectedDestination(): Boolean {
    return this?.hierarchy?.any {
        it.hasRoute<Screen.Customers>() ||
            it.hasRoute<Screen.Invoices>() ||
            it.hasRoute<Screen.Settings>() ||
            it.hasRoute<Screen.Profile>() ||
            it.hasRoute<Screen.Details>()
    } == true
}
