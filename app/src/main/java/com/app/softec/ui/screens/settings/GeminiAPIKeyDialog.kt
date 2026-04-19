package com.app.softec.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.softec.ui.theme.spacing

/**
 * OPTIONAL: API Key Configuration Dialog
 * 
 * This is a suggested enhancement for better user experience.
 * It provides a dedicated dialog for entering and validating the Gemini API key.
 * 
 * Usage in SettingsViewModel:
 * fun updateGeminiApiKey(apiKey: String) {
 *     viewModelScope.launch {
 *         settingsRepository.setGeminiApiKey(apiKey)
 *     }
 * }
 * 
 * Usage in SettingsScreen:
 * state.reminderTemplates.useAIGeneration -> show API key input option
 */

@Composable
fun GeminiAPIKeyDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey = ""

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = "Enter Gemini API Key")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = "Get your free API key from: https://aistudio.google.com/apikey",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Paste your Gemini API key here") }
                )

                Text(
                    text = "The API key will be stored securely in your device's local storage.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (apiKey.isNotBlank()) {
                        onSave(apiKey)
                        onDismiss()
                    }
                },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        }
    )
}

/**
 * INTEGRATION POINT:
 * 
 * Add to SettingsScreen in the AI Generation Section:
 * 
 * if (useAIGeneration) {
 *     var showApiKeyDialog by rememberSaveable { mutableStateOf(false) }
 *     
 *     OutlinedButton(
 *         onClick = { showApiKeyDialog = true },
 *         modifier = Modifier.fillMaxWidth()
 *     ) {
 *         Text("Configure API Key")
 *     }
 *     
 *     if (showApiKeyDialog) {
 *         GeminiAPIKeyDialog(
 *             onDismiss = { showApiKeyDialog = false },
 *             onSave = { apiKey ->
 *                 // Call ViewModel method to save API key
 *                 viewModel.updateGeminiApiKey(apiKey)
 *             }
 *         )
 *     }
 * }
 * 
 * This provides a dedicated UI for API key configuration
 * instead of embedding it in the prompt template field.
 */
