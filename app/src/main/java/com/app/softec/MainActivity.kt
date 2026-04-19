package com.app.softec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.app.softec.navigation.AppNavHost
import com.app.softec.ui.screens.settings.SettingsViewModel
import com.app.softec.ui.theme.SoftecTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            SoftecTheme(darkTheme = settingsState.isDarkModeEnabled) {
                AppNavHost(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun Greeting() {
    AppNavHost()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SoftecTheme {
        Greeting()
    }
}