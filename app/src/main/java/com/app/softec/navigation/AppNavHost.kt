package com.app.softec.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.screens.auth.AuthScreen
import com.app.softec.ui.screens.auth.SessionState
import com.app.softec.ui.screens.auth.SessionViewModel
import com.app.softec.ui.screens.home.CustomerDetailScreen
import com.app.softec.ui.screens.home.HomeDataScreen
import com.app.softec.ui.screens.invoice.InvoiceDetailScreen
import com.app.softec.ui.screens.invoice.InvoiceEditorScreen
import com.app.softec.ui.screens.invoice.InvoiceFollowUpScreen
import com.app.softec.ui.screens.invoice.InvoiceListScreen
import com.app.softec.ui.screens.settings.ReminderTemplatesScreen
import com.app.softec.ui.screens.settings.SettingsScreen
import com.app.softec.ui.screens.settings.SettingsViewModel
import com.app.softec.ui.screens.splash.HackathonSplashScreen

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
    startDestination: Screen = Screen.Splash,
    settingsViewModel: SettingsViewModel? = null
) {
    val resolvedSettingsViewModel = settingsViewModel ?: hiltViewModel<SettingsViewModel>()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val currentUserProfile by sessionViewModel.currentUserProfile.collectAsState()
    val settingsState by resolvedSettingsViewModel.uiState.collectAsState()
    val navActions = remember(navController) { NavigationActions(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var splashNavigationHandled by rememberSaveable { mutableStateOf(false) }
    var isCustomerSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedCustomersCount by rememberSaveable { mutableStateOf(0) }
    var onDeleteSelectedCustomers by remember { mutableStateOf<(() -> Unit)?>(null) }

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
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
                onSelectTab = navActions::navigateToBottomTab,
                topBarActions = {
                    if (isCustomerSelectionMode) {
                        IconButton(
                            onClick = { onDeleteSelectedCustomers?.invoke() },
                            enabled = selectedCustomersCount > 0
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete selected customers"
                            )
                        }
                    }
                }
            ) { innerPadding ->
                HomeDataScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenDetails = { itemId -> navActions.navigateTo(Screen.Details(itemId)) },
                    onSelectionStateChange = { isSelectionMode, selectedCount ->
                        isCustomerSelectionMode = isSelectionMode
                        selectedCustomersCount = selectedCount
                    },
                    onRegisterDeleteSelectedAction = { action ->
                        onDeleteSelectedCustomers = action
                    }
                )
            }
        }
        composable<Screen.Invoices> {
            TopLevelScreen(
                title = "Invoices",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                InvoiceListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    currencyPrefix = settingsState.currencyPrefix,
                    onAddInvoice = { customerId ->
                        navActions.navigateTo(Screen.AddInvoice(customerId))
                    },
                    onInvoiceClick = { accountId ->
                        navActions.navigateTo(Screen.InvoiceDetail(accountId))
                    },
                    onEditInvoice = { accountId ->
                        navActions.navigateTo(Screen.EditInvoice(accountId))
                    }
                )
            }
        }
        composable<Screen.Settings> {
            TopLevelScreen(
                title = "Settings",
                currentDestination = currentDestination,
                onSelectTab = navActions::navigateToBottomTab
            ) { innerPadding ->
                SettingsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    profile = currentUserProfile,
                    state = settingsState,
                    onToggleDarkMode = resolvedSettingsViewModel::setDarkModeEnabled,
                    onToggleCloudSync = resolvedSettingsViewModel::setCloudSyncEnabled,
                    onCurrencyPrefixChange = resolvedSettingsViewModel::setCurrencyPrefix,
                    onOpenReminderTemplates = {
                        navActions.navigateTo(Screen.ReminderTemplates)
                    },
                    onSignOut = sessionViewModel::signOut
                )
            }
        }
        composable<Screen.ReminderTemplates> {
            StandardScaffold(
                title = "Update Reminders",
                showBackButton = true,
                onNavigateBack = navActions::navigateBack
            ) { innerPadding ->
                ReminderTemplatesScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    templates = settingsState.reminderTemplates,
                    onSaveTemplates = resolvedSettingsViewModel::updateReminderTemplates,
                    onSaveApiKey = resolvedSettingsViewModel::setGeminiApiKey
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
            CustomerDetailScreen(
                customerId = details.id,
                onNavigateBack = navActions::navigateBack,
                currencyPrefix = settingsState.currencyPrefix,
                onOpenInvoice = { invoiceId ->
                    navActions.navigateTo(Screen.InvoiceDetail(invoiceId))
                }
            )
        }
        composable<Screen.InvoiceDetail> { backStackEntry ->
            val route: Screen.InvoiceDetail = backStackEntry.toRoute()
            InvoiceDetailScreen(
                invoiceId = route.id,
                onNavigateBack = navActions::navigateBack,
                currencyPrefix = settingsState.currencyPrefix,
                onFollowUpClick = { accountId ->
                    navActions.navigateTo(Screen.InvoiceFollowUp(accountId))
                }
            )
        }
        composable<Screen.InvoiceFollowUp> { backStackEntry ->
            val route: Screen.InvoiceFollowUp = backStackEntry.toRoute()
            InvoiceFollowUpScreen(
                invoiceId = route.id,
                onNavigateBack = navActions::navigateBack,
                onFollowUpSaved = navActions::navigateBack
            )
        }
        composable<Screen.AddInvoice> {
            val route: Screen.AddInvoice = it.toRoute()
            InvoiceEditorScreen(
                invoiceId = null,
                selectedCustomerId = route.customerId,
                onNavigateBack = navActions::navigateBack
            )
        }
        composable<Screen.EditInvoice> { backStackEntry ->
            val route: Screen.EditInvoice = backStackEntry.toRoute()
            InvoiceEditorScreen(
                invoiceId = route.id,
                selectedCustomerId = null,
                onNavigateBack = navActions::navigateBack
            )
        }
    }
}

@Composable
private fun TopLevelScreen(
    title: String,
    currentDestination: NavDestination?,
    onSelectTab: (Screen) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val currentTabIndex = currentDestination.topLevelTabIndex()
    StandardScaffold(
        title = title,
        topBarActions = topBarActions,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentTabIndex) {
                    var totalHorizontalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            totalHorizontalDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                totalHorizontalDrag <= -80f -> {
                                    val nextIndex = currentTabIndex + 1
                                    topLevelDestinations.getOrNull(nextIndex)?.screen?.let(onSelectTab)
                                }
                                totalHorizontalDrag >= 80f -> {
                                    val previousIndex = currentTabIndex - 1
                                    topLevelDestinations.getOrNull(previousIndex)?.screen?.let(onSelectTab)
                                }
                            }
                            totalHorizontalDrag = 0f
                        }
                    )
                }
        ) {
            content(innerPadding)
        }
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
        Screen.Customers -> this?.hierarchy?.any { it.hasRoute<Screen.Customers>() } == true
        Screen.Invoices -> this?.hierarchy?.any { it.hasRoute<Screen.Invoices>() } == true
        Screen.Settings -> this?.hierarchy?.any { it.hasRoute<Screen.Settings>() } == true
        Screen.Profile -> this?.hierarchy?.any { it.hasRoute<Screen.Profile>() } == true
        else -> false
    }
}

private fun NavDestination?.topLevelTabIndex(): Int {
    return when {
        this?.hierarchy?.any { it.hasRoute<Screen.Customers>() } == true -> 0
        this?.hierarchy?.any { it.hasRoute<Screen.Invoices>() } == true -> 1
        this?.hierarchy?.any { it.hasRoute<Screen.Settings>() } == true -> 2
        else -> -1
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
            it.hasRoute<Screen.ReminderTemplates>() ||
            it.hasRoute<Screen.Profile>() ||
            it.hasRoute<Screen.Details>() ||
            it.hasRoute<Screen.InvoiceDetail>() ||
            it.hasRoute<Screen.InvoiceFollowUp>() ||
            it.hasRoute<Screen.AddInvoice>() ||
            it.hasRoute<Screen.EditInvoice>()
    } == true
}
