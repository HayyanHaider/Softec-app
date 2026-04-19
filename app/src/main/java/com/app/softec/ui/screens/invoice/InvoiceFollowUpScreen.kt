package com.app.softec.ui.screens.invoice

import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.theme.spacing
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvoiceFollowUpScreen(
    invoiceId: String,
    onNavigateBack: () -> Unit,
    onFollowUpSaved: () -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.followUpState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(invoiceId) {
        viewModel.prepareFollowUp(invoiceId)
    }

    LaunchedEffect(state.submissionCompleted) {
        if (state.submissionCompleted) {
            viewModel.clearFollowUpCompletion()
            onFollowUpSaved()
        }
    }

    StandardScaffold(
        title = "Follow Up",
        showBackButton = true,
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null && state.accountId == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage ?: "Unable to load follow-up data.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    FollowUpSectionCard(title = "Customer") {
                        Text(
                            text = state.customerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            FollowUpInfoPill(
                                icon = Icons.Default.Phone,
                                text = state.contactNumber.ifBlank { "No phone" }
                            )
                            FollowUpInfoPill(
                                icon = Icons.Default.Email,
                                text = state.customerEmail ?: "No email"
                            )
                        }
                    }

                    FollowUpSectionCard(title = "Send Via") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            FilterChip(
                                selected = state.selectedChannel == FollowUpContactChannel.WHATSAPP,
                                onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.WHATSAPP) },
                                label = { Text("WhatsApp") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.selectedChannel == FollowUpContactChannel.EMAIL,
                                onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.EMAIL) },
                                label = { Text("Mail") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.selectedChannel == FollowUpContactChannel.SMS,
                                onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.SMS) },
                                label = { Text("SMS") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                        }
                    }

                    FollowUpSectionCard(title = "Reminder Message") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            OutlinedTextField(
                                value = state.draftMessage,
                                onValueChange = viewModel::updateDraftMessage,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                                placeholder = {
                                    Text("Type follow-up message")
                                }
                            )

                            PrimaryButton(
                                text = if (state.isGeneratingAI) "Generating..." else "Generate with AI",
                                onClick = { viewModel.generateAIMessage() },
                                enabled = !state.isGeneratingAI,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (state.aiGeneratedMessage != null && state.aiGeneratedMessage != state.suggestedMessage) {
                                Text(
                                    text = "✓ AI message generated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (state.errorMessage?.contains("AI", ignoreCase = true) == true) {
                                Text(
                                    text = state.errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    FollowUpSectionCard(title = "Schedule Next Follow-Up") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            FilterChip(
                                selected = state.selectedIntervalDays == 1,
                                onClick = { viewModel.updateFollowUpInterval(1) },
                                label = { Text("1 Day") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.selectedIntervalDays == 3,
                                onClick = { viewModel.updateFollowUpInterval(3) },
                                label = { Text("3 Days") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.selectedIntervalDays == 7,
                                onClick = { viewModel.updateFollowUpInterval(7) },
                                label = { Text("1 Week") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.selectedIntervalDays == null,
                                onClick = { viewModel.updateFollowUpInterval(null) },
                                label = { Text("Auto") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                            FilterChip(
                                selected = state.isCustomDateSelected,
                                onClick = {
                                    showFollowUpDatePicker(
                                        context = context,
                                        currentDate = state.nextFollowUpDate,
                                        onDateSelected = viewModel::updateFollowUpDate
                                    )
                                },
                                label = { Text("Pick Date") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                                    )
                                }
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(MaterialTheme.spacing.small),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Next Follow-Up",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = state.nextFollowUpDate.toBusinessDateLabel(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    PrimaryButton(
                        text = if (state.isSubmitting) "Scheduling..." else "Schedule Follow Up",
                        onClick = {
                            val launched = launchFollowUpIntent(context, state)
                            if (launched) {
                                viewModel.submitFollowUp()
                            } else {
                                viewModel.setFollowUpError(
                                    "No compatible app found for ${state.selectedChannel.value}."
                                )
                            }
                        },
                        enabled = !state.isSubmitting
                    )
                }
            }
        }
    }
}

private fun showFollowUpDatePicker(
    context: Context,
    currentDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        time = currentDate ?: Date()
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1_000L
    }.show()
}

@Composable
private fun FollowUpSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                content()
            }
        )
    }
}

@Composable
private fun FollowUpInfoPill(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.extraSmall
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.spacing.medium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun java.util.Date?.toBusinessDateLabel(): String {
    if (this == null) return "Not required"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(this)
}

private fun launchFollowUpIntent(context: Context, state: FollowUpUiState): Boolean {
    val message = state.draftMessage.ifBlank { state.suggestedMessage }
    return when (state.selectedChannel) {
        FollowUpContactChannel.WHATSAPP -> launchWhatsAppIntent(context, state.contactNumber, message)
        FollowUpContactChannel.SMS -> launchSmsIntent(context, state.contactNumber, message)
        FollowUpContactChannel.EMAIL -> launchEmailIntent(context, state.customerEmail.orEmpty(), message)
    }
}

private fun launchWhatsAppIntent(context: Context, contactNumber: String, message: String): Boolean {
    if (contactNumber.isBlank()) return false

    val digits = contactNumber.filter { it.isDigit() }
    if (digits.isBlank()) return false

    val encodedMessage = Uri.encode(message)

    // Try consumer and business packages first, then generic deep links as fallback.
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=$encodedMessage")).apply {
            setPackage("com.whatsapp")
        },
        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=$encodedMessage")).apply {
            setPackage("com.whatsapp.w4b")
        },
        Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$digits&text=$encodedMessage")).apply {
            setPackage("com.whatsapp")
        },
        Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$digits&text=$encodedMessage")).apply {
            setPackage("com.whatsapp.w4b")
        },
        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=$encodedMessage")),
        Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$digits&text=$encodedMessage"))
    )

    return intents.any { tryStartActivity(context, it) }
}

private fun launchSmsIntent(context: Context, contactNumber: String, message: String): Boolean {
    if (contactNumber.isBlank()) return false

    val uri = Uri.parse("smsto:${Uri.encode(contactNumber)}")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra("sms_body", message)
    }
    return tryStartActivity(context, intent)
}

private fun launchEmailIntent(context: Context, email: String, message: String): Boolean {
    if (email.isBlank()) return false

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, "Payment Follow Up")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    return tryStartActivity(context, intent)
}

private fun tryStartActivity(context: Context, intent: Intent): Boolean {
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
