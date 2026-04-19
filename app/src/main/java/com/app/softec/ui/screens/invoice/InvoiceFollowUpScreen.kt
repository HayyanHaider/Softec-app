package com.app.softec.ui.screens.invoice

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.theme.spacing
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

            state.errorMessage != null -> {
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
                    Text(
                        text = "Customer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.customerName,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Contact: ${state.contactNumber.ifBlank { "N/A" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Email: ${state.customerEmail ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Send Via",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        FilterChip(
                            selected = state.selectedChannel == FollowUpContactChannel.WHATSAPP,
                            onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.WHATSAPP) },
                            label = { Text("WhatsApp") }
                        )
                        FilterChip(
                            selected = state.selectedChannel == FollowUpContactChannel.SMS,
                            onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.SMS) },
                            label = { Text("SMS") }
                        )
                        FilterChip(
                            selected = state.selectedChannel == FollowUpContactChannel.EMAIL,
                            onClick = { viewModel.updateFollowUpChannel(FollowUpContactChannel.EMAIL) },
                            label = { Text("Email") }
                        )
                    }

                    Text(
                        text = "Reminder Message",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = state.draftMessage,
                        onValueChange = viewModel::updateDraftMessage,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        placeholder = {
                            Text("Enter a professional follow-up message")
                        }
                    )

                    Text(
                        text = "Schedule Next Follow-Up",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        AssistChip(
                            onClick = { viewModel.updateFollowUpInterval(1) },
                            label = { Text("+1 day") }
                        )
                        AssistChip(
                            onClick = { viewModel.updateFollowUpInterval(3) },
                            label = { Text("+3 days") }
                        )
                        AssistChip(
                            onClick = { viewModel.updateFollowUpInterval(7) },
                            label = { Text("+7 days") }
                        )
                        AssistChip(
                            onClick = { viewModel.updateFollowUpInterval(null) },
                            label = { Text("Auto") }
                        )
                    }

                    Text(
                        text = "Next Follow-Up Date: ${state.nextFollowUpDate.toBusinessDateLabel()}",
                        style = MaterialTheme.typography.bodyLarge
                    )

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
