package com.app.softec.ui.screens.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.EmptyState
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.ui.theme.spacing
import java.text.NumberFormat

@Composable
fun InvoiceListScreen(
    modifier: Modifier = Modifier,
    onAddInvoice: (String) -> Unit,
    onInvoiceClick: (String) -> Unit,
    onEditInvoice: (String) -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsState()
    val errorMessage = state.errorMessage
    var invoiceToDelete by remember { mutableStateOf<InvoiceListItemUi?>(null) }
    var showCustomerSelector by rememberSaveable { mutableStateOf(false) }
    var showMissingCustomersDialog by rememberSaveable { mutableStateOf(false) }
    var attemptedRetry by rememberSaveable { mutableStateOf(false) }

    val openAddInvoiceSelector = {
        if (state.availableCustomers.isEmpty()) {
            showMissingCustomersDialog = true
        } else {
            showCustomerSelector = true
        }
    }

    invoiceToDelete?.let { selected ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Delete Invoice") },
            text = {
                Text(
                    "Do you want to permanently delete this invoice for ${selected.customerName}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInvoice(selected.account)
                        invoiceToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMissingCustomersDialog) {
        AlertDialog(
            onDismissRequest = { showMissingCustomersDialog = false },
            title = { Text("No Customers Available") },
            text = {
                Text("Add at least one customer before creating an invoice.")
            },
            confirmButton = {
                TextButton(onClick = { showMissingCustomersDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showCustomerSelector) {
        AlertDialog(
            onDismissRequest = { showCustomerSelector = false },
            title = { Text("Select Customer") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    state.availableCustomers.forEach { customer ->
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showCustomerSelector = false
                                onAddInvoice(customer.customerId)
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = customer.customerName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (customer.contactNumber.isNotBlank()) {
                                    Text(
                                        text = customer.contactNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerSelector = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null && state.invoices.isEmpty() -> {
            EmptyState(
                modifier = modifier,
                title = "Unable to load invoices",
                message = errorMessage,
                onAction = {
                    attemptedRetry = true
                    viewModel.observeInvoiceList()
                },
                actionText = if (attemptedRetry) "Retry again" else "Retry"
            )
        }

        state.invoices.isEmpty() -> {
            EmptyState(
                modifier = modifier,
                title = "No invoices available",
                message = "Use Add Invoice to register a new account receivable for an existing customer.",
                onAction = openAddInvoiceSelector,
                actionText = "Add Invoice"
            )
        }

        else -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                PrimaryButton(
                    text = "Add Invoice",
                    onClick = openAddInvoiceSelector
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    item {
                        InvoiceSummaryCard(
                            overdueCount = state.overdueCount,
                            highPriorityCount = state.highPriorityCount,
                            totalOutstanding = state.totalOutstanding
                        )
                    }

                    items(
                        items = state.invoices,
                        key = { it.account.id }
                    ) { invoice ->
                        InvoiceItem(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.account.id) },
                            onEdit = { onEditInvoice(invoice.account.id) },
                            onDelete = { invoiceToDelete = invoice }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    overdueCount: Int,
    highPriorityCount: Int,
    totalOutstanding: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spacing.extraSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Text(
                text = "Collections Priority Snapshot",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Overdue accounts: $overdueCount",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "High-priority (30+ days): $highPriorityCount",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Outstanding balance: ${NumberFormat.getCurrencyInstance().format(totalOutstanding)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
