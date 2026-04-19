package com.app.softec.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.ui.theme.spacing

@Composable
fun ReminderTemplatesScreen(
    modifier: Modifier = Modifier,
    templates: ReminderTemplates,
    onSaveTemplates: (ReminderTemplates) -> Unit
) {
    var friendlyTemplate by rememberSaveable(templates.friendly) { mutableStateOf(templates.friendly) }
    var standardTemplate by rememberSaveable(templates.standard) { mutableStateOf(templates.standard) }
    var urgentTemplate by rememberSaveable(templates.urgent) { mutableStateOf(templates.urgent) }
    var remindersUpdated by rememberSaveable { mutableStateOf(false) }

    val updatedTemplates = ReminderTemplates(
        friendly = friendlyTemplate.trim(),
        standard = standardTemplate.trim(),
        urgent = urgentTemplate.trim()
    )
    val canSave =
        updatedTemplates.friendly.isNotBlank() &&
            updatedTemplates.standard.isNotBlank() &&
            updatedTemplates.urgent.isNotBlank() &&
            updatedTemplates != templates

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
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
