package com.app.softec.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.ui.screens.auth.SessionUserProfile
import com.app.softec.ui.theme.spacing
import coil.compose.AsyncImage
import kotlin.math.min

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profile: SessionUserProfile?,
    state: SettingsUiState,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleCloudSync: (Boolean) -> Unit,
    onCurrencyPrefixChange: (String) -> Unit,
    onOpenReminderTemplates: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MaterialTheme.spacing.extraLarge * 3)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                    ) {
                        ProfileAvatar(profile = profile)

                        Text(
                            text = profile?.username ?: "User",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            SettingToggleCard(
                title = "Dark Mode",
                checked = state.isDarkModeEnabled,
                onCheckedChange = onToggleDarkMode
            )

            SettingToggleCard(
                title = "Toggle Cloud Sync",
                checked = state.isCloudSyncEnabled,
                onCheckedChange = onToggleCloudSync
            )

            CurrencyPrefixCard(
                prefix = state.currencyPrefix,
                onPrefixChange = onCurrencyPrefixChange
            )

            SettingActionCard(
                title = "Update Reminders",
                onClick = onOpenReminderTemplates
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Sign out",
            onClick = onSignOut,
            horizontalPadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun CurrencyPrefixCard(
    prefix: String,
    onPrefixChange: (String) -> Unit
) {
    var inputValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = prefix,
                selection = TextRange(prefix.length)
            )
        )
    }

    LaunchedEffect(prefix) {
        if (prefix != inputValue.text) {
            inputValue = TextFieldValue(
                text = prefix,
                selection = TextRange(prefix.length)
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Currency Prefix",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = MaterialTheme.spacing.small)
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedTextField(
                modifier = Modifier.widthIn(max = MaterialTheme.spacing.extraLarge * 4),
                value = inputValue,
                onValueChange = { nextValue ->
                    val limitedValue = nextValue.limitTo3Chars()
                    inputValue = limitedValue
                    onPrefixChange(limitedValue.text)
                },
                singleLine = true,
                label = { Text("Prefix") },
                isError = inputValue.text.isBlank(),
                supportingText = {
                    if (inputValue.text.isBlank()) {
                        Text("Currency prefix is required")
                    }
                }
            )
        }
    }
}

private fun TextFieldValue.limitTo3Chars(): TextFieldValue {
    if (text.length <= 3) {
        return this
    }
    val limitedText = text.take(3)
    val limitedStart = min(selection.start, 3)
    val limitedEnd = min(selection.end, 3)
    return copy(
        text = limitedText,
        selection = TextRange(limitedStart, limitedEnd)
    )
}

@Composable
private fun SettingActionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(onClick) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ProfileAvatar(profile: SessionUserProfile?) {
    val avatarText = (profile?.email?.firstOrNull() ?: profile?.username?.firstOrNull() ?: 'U')
        .uppercaseChar()
        .toString()

    if (!profile?.photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = profile?.photoUrl,
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(MaterialTheme.spacing.extraLarge * 2)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(MaterialTheme.spacing.extraLarge * 2)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Card {
                Box(
                    modifier = Modifier
                        .size(MaterialTheme.spacing.extraLarge * 2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarText,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}