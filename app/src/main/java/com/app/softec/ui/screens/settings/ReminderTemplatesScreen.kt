package com.app.softec.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.ui.theme.spacing

@Composable
fun ReminderTemplatesScreen(
    modifier: Modifier = Modifier,
    templates: ReminderTemplates,
    onSaveTemplates: (ReminderTemplates) -> Unit,
    onSaveApiKey: (String) -> Unit = {}
) {
    var friendlyTemplate by rememberSaveable(templates.friendly) { mutableStateOf(templates.friendly) }
    var standardTemplate by rememberSaveable(templates.standard) { mutableStateOf(templates.standard) }
    var urgentTemplate by rememberSaveable(templates.urgent) { mutableStateOf(templates.urgent) }
    var useAIGeneration by rememberSaveable(templates.useAIGeneration) { mutableStateOf(templates.useAIGeneration) }
    var aiPromptTemplate by rememberSaveable(templates.aiPromptTemplate) { mutableStateOf(templates.aiPromptTemplate) }
    var geminiApiKey by rememberSaveable { mutableStateOf("") }
    var remindersUpdated by rememberSaveable { mutableStateOf(false) }

    val updatedTemplates = ReminderTemplates(
        friendly = friendlyTemplate.trim(),
        standard = standardTemplate.trim(),
        urgent = urgentTemplate.trim(),
        useAIGeneration = useAIGeneration,
        aiPromptTemplate = aiPromptTemplate.trim()
    )
    val canSave =
        updatedTemplates.friendly.isNotBlank() &&
            updatedTemplates.standard.isNotBlank() &&
            updatedTemplates.urgent.isNotBlank() &&
            (updatedTemplates != templates)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        // Manual Templates Section
        Text(
            text = "Manual Templates",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = friendlyTemplate,
            onValueChange = {
                friendlyTemplate = keepPlaceholdersImmutable(
                    previousValue = friendlyTemplate,
                    candidateValue = it
                )
                remindersUpdated = false
            },
            label = { Text("Friendly") },
            minLines = 3
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = standardTemplate,
            onValueChange = {
                standardTemplate = keepPlaceholdersImmutable(
                    previousValue = standardTemplate,
                    candidateValue = it
                )
                remindersUpdated = false
            },
            label = { Text("Standard") },
            minLines = 3
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = urgentTemplate,
            onValueChange = {
                urgentTemplate = keepPlaceholdersImmutable(
                    previousValue = urgentTemplate,
                    candidateValue = it
                )
                remindersUpdated = false
            },
            label = { Text("Urgent") },
            minLines = 3
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

        // AI Generation Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Text(
                text = "AI Message Generation",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Enable AI-powered message generation for more personalized follow-ups",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Switch(
                    checked = useAIGeneration,
                    onCheckedChange = {
                        useAIGeneration = it
                        remindersUpdated = false
                    }
                )
            }
        }

        if (useAIGeneration) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = aiPromptTemplate,
                onValueChange = {
                    aiPromptTemplate = it
                    remindersUpdated = false
                },
                label = { Text("AI Prompt Template") },
                minLines = 4,
                placeholder = { Text("Use {name}, {amount}, {urgency} as placeholders") }
            )

            Text(
                text = "Available variables: {name}, {amount}, {urgency}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = MaterialTheme.spacing.small)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

            Text(
                text = "Gemini API Key",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = geminiApiKey,
                onValueChange = { geminiApiKey = it },
                label = { Text("API Key") },
                placeholder = { Text("Paste your Gemini API key here") },
                singleLine = true
            )

            Text(
                text = "Get free API key: https://aistudio.google.com/apikey",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
            )

            PrimaryButton(
                text = "Save API Key",
                onClick = {
                    if (geminiApiKey.isNotBlank()) {
                        onSaveApiKey(geminiApiKey)
                        geminiApiKey = ""
                        remindersUpdated = true
                    }
                },
                enabled = geminiApiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        PrimaryButton(
            text = "Save Templates",
            onClick = {
                onSaveTemplates(updatedTemplates)
                remindersUpdated = true
            },
            enabled = canSave
        )

        if (remindersUpdated) {
            Text(
                text = "Reminders updated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private val placeholderRegex = Regex("\\{[^{}]+\\}")

private fun keepPlaceholdersImmutable(
    previousValue: String,
    candidateValue: String
): String {
    val previousPlaceholders = placeholderRegex.findAll(previousValue)
        .map { it.value }
        .toList()
    val candidatePlaceholders = placeholderRegex.findAll(candidateValue)
        .map { it.value }
        .toList()
    return if (candidatePlaceholders == previousPlaceholders) {
        candidateValue
    } else {
        previousValue
    }
}
