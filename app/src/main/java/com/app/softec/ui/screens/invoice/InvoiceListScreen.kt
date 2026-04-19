package com.app.softec.ui.screens.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.formatCurrencyWithPrefix
import com.app.softec.core.ui.components.EmptyState
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.domain.usecase.InvoiceFilterType
import com.app.softec.ui.theme.spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvoiceListScreen(
    modifier: Modifier = Modifier,
    currencyPrefix: String,
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
    var selectedFilter by rememberSaveable { mutableStateOf(InvoiceFilterType.ALL) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var showDeleteSelectedDialog by rememberSaveable { mutableStateOf(false) }
    val selectedInvoiceIds = remember { mutableStateListOf<String>() }

    val filteredInvoices = remember(state.invoices, selectedFilter) {
        state.invoices.filter { invoice -> invoice.matchesFilter(selectedFilter) }
    }

    LaunchedEffect(selectionMode, filteredInvoices) {
        if (!selectionMode) {
            selectedInvoiceIds.clear()
        } else {
            val visibleIds = filteredInvoices.map { it.account.id }.toSet()
            selectedInvoiceIds.removeAll { it !in visibleIds }
        }
    }

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

    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text("Delete Invoices") },
            text = {
                Text("Delete ${selectedInvoiceIds.size} selected invoices?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedAccounts = state.invoices
                            .filter { it.account.id in selectedInvoiceIds }
                            .map { it.account }

                        selectedAccounts.forEach { account ->
                            viewModel.deleteInvoice(account)
                        }

                        selectedInvoiceIds.clear()
                        selectionMode = false
                        showDeleteSelectedDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryButton(
                        text = "Add Invoice",
                        onClick = openAddInvoiceSelector,
                        modifier = Modifier.weight(1f),
                        horizontalPadding = 0.dp
                    )
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    InvoiceFilterType.entries.forEach { filterType ->
                        FilterChip(
                            selected = selectedFilter == filterType,
                            onClick = { selectedFilter = filterType },
                            label = { Text(filterType.displayName()) }
                        )
                    }
                }

                if (selectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedInvoiceIds.size} selected",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { selectionMode = false }
                            ) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = { showDeleteSelectedDialog = true },
                                enabled = selectedInvoiceIds.isNotEmpty()
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    item {
                        InvoiceSummaryCard(
                            invoices = state.invoices,
                            totalOutstanding = state.totalOutstanding,
                            currencyPrefix = currencyPrefix
                        )
                    }

                    if (filteredInvoices.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Text(
                                    text = "No invoices in ${selectedFilter.displayName()} filter.",
                                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = filteredInvoices,
                            key = { it.account.id }
                        ) { invoice ->
                            val isSelected = invoice.account.id in selectedInvoiceIds
                            InvoiceItem(
                                invoice = invoice,
                                onClick = { onInvoiceClick(invoice.account.id) },
                                onLongPress = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedInvoiceIds.clear()
                                        selectedInvoiceIds.add(invoice.account.id)
                                    }
                                },
                                onEdit = { onEditInvoice(invoice.account.id) },
                                onDelete = { invoiceToDelete = invoice },
                                currencyPrefix = currencyPrefix,
                                selectionMode = selectionMode,
                                selected = isSelected,
                                onSelectionToggle = {
                                    if (isSelected) {
                                        selectedInvoiceIds.remove(invoice.account.id)
                                    } else {
                                        selectedInvoiceIds.add(invoice.account.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceSummaryCard(
    invoices: List<InvoiceListItemUi>,
    totalOutstanding: Double,
    currencyPrefix: String
) {
    val actionableInvoices = invoices.filter { it.account.amountRemaining > 0.0 }
    val overdueCount = actionableInvoices.count {
        it.account.status.equals("overdue", ignoreCase = true) || it.account.daysOverdue > 0
    }
    val highPriorityCount = actionableInvoices.count { it.account.daysOverdue >= 30 }
    val mediumPriorityCount = actionableInvoices.count { it.account.daysOverdue in 8..29 }
    val lowPriorityCount =
        (actionableInvoices.size - highPriorityCount - mediumPriorityCount).coerceAtLeast(0)

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
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                InvoiceSnapshotPill(
                    modifier = Modifier.weight(1f),
                    label = "Outstanding",
                    value = formatCurrencyWithPrefix(totalOutstanding, currencyPrefix),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                InvoiceSnapshotPill(
                    modifier = Modifier.weight(1f),
                    label = "Overdue",
                    value = overdueCount.toString(),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                InvoiceSnapshotPill(
                    modifier = Modifier.weight(1f),
                    label = "Low",
                    value = lowPriorityCount.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                InvoiceSnapshotPill(
                    modifier = Modifier.weight(1f),
                    label = "Medium",
                    value = mediumPriorityCount.toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                InvoiceSnapshotPill(
                    modifier = Modifier.weight(1f),
                    label = "High",
                    value = highPriorityCount.toString(),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun InvoiceSnapshotPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.small,
                    vertical = MaterialTheme.spacing.extraSmall
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun InvoiceListItemUi.matchesFilter(filterType: InvoiceFilterType): Boolean {
    val normalizedStatus = account.status.lowercase()
    return when (filterType) {
        InvoiceFilterType.ALL -> true
        InvoiceFilterType.ACTIVE -> normalizedStatus == "active"
        InvoiceFilterType.OVERDUE -> normalizedStatus == "overdue" || account.daysOverdue > 0
        InvoiceFilterType.PARTIAL -> normalizedStatus == "partial"
        InvoiceFilterType.PAID -> normalizedStatus == "paid"
    }
}

private fun InvoiceFilterType.displayName(): String {
    return when (this) {
        InvoiceFilterType.ALL -> "All"
        InvoiceFilterType.ACTIVE -> "Active"
        InvoiceFilterType.OVERDUE -> "Overdue"
        InvoiceFilterType.PARTIAL -> "Partial"
        InvoiceFilterType.PAID -> "Paid"
    }
}
