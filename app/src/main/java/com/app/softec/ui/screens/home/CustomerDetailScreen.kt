package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.theme.spacing

@Composable
fun CustomerDetailScreen(
    customerId: String,
    onNavigateBack: () -> Unit,
    onOpenInvoice: (String) -> Unit,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsState()
    LaunchedEffect(customerId) { viewModel.loadCustomerDetail(customerId) }
    StandardScaffold(
        title = "Customer Details",
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

            state.errorMessage != null || state.customer == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage ?: "Customer not found.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> CustomerDetailContent(
                state = state,
                onOpenInvoice = onOpenInvoice,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun CustomerDetailContent(
    state: CustomerDetailUiState,
    onOpenInvoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customer = state.customer ?: return
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
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
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = customer.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Last Updated: ${customer.updatedAt.toBusinessDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DetailMetricCard("Phone", state.phone.ifBlank { "Not provided" })
        DetailMetricCard("Email", state.email.ifBlank { "Not provided" })
        DetailMetricCard("Notes", customer.notes?.takeIf { it.isNotBlank() } ?: "No notes")
        DetailMetricCard(
            "Tags",
            customer.tags.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "No tags"
        )
        CustomerInvoiceList(
            invoices = state.invoices,
            onInvoiceClick = onOpenInvoice
        )
        DetailMetricCard("Total Amount Due", state.totalAmountDue.toBusinessCurrency())
        DetailMetricCard("Amount Paid", state.totalAmountPaid.toBusinessCurrency())
        DetailMetricCard("Amount Remaining", state.totalAmountRemaining.toBusinessCurrency())
        DetailMetricCard("Overdue Invoices", state.overdueInvoicesCount.toString())
    }
}
