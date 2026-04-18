package com.app.softec.ui.screens.invoices

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.EmptyState
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StatefulContent
import com.app.softec.core.ui.components.UiState
import com.app.softec.domain.usecase.GetOverdueAccountsUseCase
import com.app.softec.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OverdueAccountsScreen(
    modifier: Modifier = Modifier,
    viewModel: OverdueAccountsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<Date?>(null) }

    StatefulContent(
        state = state,
        modifier = modifier.fillMaxSize()
    ) { data ->
        if (data.accounts.isEmpty()) {
            EmptyState(
                title = "No Overdue Accounts",
                message = "All accounts are current. Great job!"
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                // Sort toggle
                SortToggle(
                    currentSort = data.sortOption,
                    onSortChanged = viewModel::changeSortOption
                )

                // Error display
                if (!data.lastError.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = data.lastError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(MaterialTheme.spacing.medium)
                        )
                    }
                }

                // Accounts list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    items(data.accounts, key = { it.account.id }) { accountWithCustomer ->
                        OverdueAccountCard(
                            account = accountWithCustomer.account,
                            customerName = accountWithCustomer.customer?.customerName ?: "Unknown Customer",
                            contactNumber = accountWithCustomer.customer?.contactNumber,
                            onScheduleFollowUp = {
                                selectedDate = null
                                viewModel.onScheduleFollowUp(accountWithCustomer.account.id)
                            },
                            onCallClick = {
                                accountWithCustomer.customer?.contactNumber?.let { phone ->
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle gracefully
                                    }
                                }
                            },
                            onWhatsAppClick = {
                                accountWithCustomer.customer?.contactNumber?.let { phone ->
                                    val message = "Hi, this is a follow-up regarding your account."
                                    val encodedMessage = Uri.encode(message)
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/$phone?text=$encodedMessage")
                                    )
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle gracefully
                                    }
                                }
                            },
                            onSmsClick = {
                                accountWithCustomer.customer?.contactNumber?.let { phone ->
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle gracefully
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Date picker dialog
            if (data.showDatePicker && data.selectedFollowUpDate != null) {
                DatePickerDialog(
                    initialDate = data.selectedFollowUpDate,
                    suggestedDate = data.suggestedFollowUpDate,
                    onDateSelected = viewModel::onDateSelected,
                    onConfirm = viewModel::onConfirmSchedule,
                    onDismiss = viewModel::onDismissDatePicker,
                    isSyncing = data.syncingInProgress
                )
            }
        }
    }
}

@Composable
private fun SortToggle(
    currentSort: GetOverdueAccountsUseCase.SortOption,
    onSortChanged: (GetOverdueAccountsUseCase.SortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort by:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(end = MaterialTheme.spacing.small)
        )

        OutlinedButton(
            onClick = {
                if (currentSort != GetOverdueAccountsUseCase.SortOption.DAYS_OVERDUE_DESC) {
                    onSortChanged(GetOverdueAccountsUseCase.SortOption.DAYS_OVERDUE_DESC)
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Days Overdue",
                style = MaterialTheme.typography.labelSmall
            )
        }

        OutlinedButton(
            onClick = {
                if (currentSort != GetOverdueAccountsUseCase.SortOption.AMOUNT_DESC) {
                    onSortChanged(GetOverdueAccountsUseCase.SortOption.AMOUNT_DESC)
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Amount Due",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun OverdueAccountCard(
    account: com.app.softec.domain.model.Account,
    customerName: String,
    contactNumber: String?,
    onScheduleFollowUp: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.extraSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "Account ${account.id.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

            // Amount and days overdue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", account.amountRemaining)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Days Overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${account.daysOverdue} days",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.small))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact action buttons
                if (!contactNumber.isNullOrBlank()) {
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call"
                        )
                    }

                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "WhatsApp"
                        )
                    }

                    IconButton(
                        onClick = onSmsClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "SMS"
                        )
                    }

                    PrimaryButton(
                        text = "Schedule",
                        onClick = onScheduleFollowUp,
                        modifier = Modifier.weight(1.5f)
                    )
                } else {
                    PrimaryButton(
                        text = "Schedule Follow-up",
                        onClick = onScheduleFollowUp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerDialog(
    initialDate: Date,
    suggestedDate: Date?,
    onDateSelected: (Date) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isSyncing: Boolean
) {
    // Simple date picker dialog using basic Compose components
    // In production, you'd use Material3's DatePicker or Android's DatePickerDialog
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(
                text = if (isSyncing) "Syncing..." else "Confirm",
                onClick = onConfirm,
                enabled = !isSyncing
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSyncing
            ) {
                Text("Cancel")
            }
        },
        title = {
            Text("Schedule Follow-up")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                if (suggestedDate != null) {
                    Text(
                        text = "Suggested date: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(suggestedDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Selected date: ${SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(initialDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // In a full implementation, add a Material3 DatePicker or calendar view here
                // For now, this dialog shows the selected date and allows confirmation
                Text(
                    text = "Date selection UI would go here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
