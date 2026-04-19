package com.app.softec.ui.screens.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.formatCurrencyWithPrefix
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.domain.model.Account
import com.app.softec.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onNavigateBack: () -> Unit,
    currencyPrefix: String,
    onFollowUpClick: (String) -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsState()
    val account = state.account

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoiceDetail(invoiceId)
    }

    StandardScaffold(
        title = "Invoice Details",
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

            state.errorMessage != null || account == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage ?: "Invoice not found.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                InvoiceDetailContent(
                    account = account,
                    customerName = state.customerName,
                    currencyPrefix = currencyPrefix,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onFollowUpClick = { onFollowUpClick(account.id) }
                )
            }
        }
    }
}

@Composable
private fun InvoiceDetailContent(
    account: Account,
    customerName: String,
    currencyPrefix: String,
    modifier: Modifier = Modifier,
    onFollowUpClick: () -> Unit
) {
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
                    text = customerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                InvoiceDetailStatusPill(status = account.status)
                Text(
                    text = "Due Date: ${account.dueDate.toBusinessDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        InvoiceMetricCard(
            title = "Total Amount Due",
            value = formatCurrencyWithPrefix(account.totalAmountDue, currencyPrefix)
        )
        InvoiceMetricCard(
            title = "Amount Paid",
            value = formatCurrencyWithPrefix(account.amountPaid, currencyPrefix)
        )
        InvoiceMetricCard(
            title = "Amount Remaining",
            value = formatCurrencyWithPrefix(account.amountRemaining, currencyPrefix)
        )
        InvoiceMetricCard(
            title = "Days Overdue",
            value = account.daysOverdue.toString()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        PrimaryButton(
            text = "Follow Up",
            onClick = onFollowUpClick
        )
    }
}

@Composable
private fun InvoiceDetailStatusPill(status: String) {
    val normalizedStatus = status.lowercase()
    val (containerColor, contentColor) = when (normalizedStatus) {
        "paid" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "overdue" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "partial" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = normalizedStatus.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.extraSmall
            )
        )
    }
}

@Composable
private fun InvoiceMetricCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun java.util.Date.toBusinessDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(this)
}
