package com.app.softec.ui.screens.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import com.app.softec.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceItem(
    invoice: InvoiceListItemUi,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }

                SwipeToDismissBoxValue.Settled -> true
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val (icon, backgroundColor, alignment) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(
                    Icons.Default.Edit,
                    MaterialTheme.colorScheme.primary,
                    Alignment.CenterStart
                )

                SwipeToDismissBoxValue.EndToStart -> Triple(
                    Icons.Default.Delete,
                    MaterialTheme.colorScheme.error,
                    Alignment.CenterEnd
                )

                SwipeToDismissBoxValue.Settled -> Triple(
                    Icons.Default.Edit,
                    Color.Transparent,
                    Alignment.CenterStart
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = MaterialTheme.spacing.medium),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Due: ${invoice.account.dueDate.toBusinessDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
                    )
                }

                Text(
                    text = invoice.account.amountRemaining.toBusinessCurrency(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun Double.toBusinessCurrency(): String {
    return NumberFormat.getCurrencyInstance().format(this)
}

private fun java.util.Date.toBusinessDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(this)
}
