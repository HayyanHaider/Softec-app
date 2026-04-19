package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.softec.core.ui.components.EmptyState
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StatefulContent
import com.app.softec.core.ui.components.UiState
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.ui.theme.spacing

@Composable
fun HomeCustomerListContent(
    state: UiState<List<SyncItemEntity>>,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    content: @Composable (List<SyncItemEntity>) -> Unit
) {
    StatefulContent(
        state = state,
        modifier = modifier.fillMaxSize(),
        onRetry = onRetry,
        content = content
    )
}

@Composable
fun CustomerRowsContent(
    items: List<SyncItemEntity>,
    isSelectionMode: Boolean,
    selectedCustomerIds: Set<String>,
    onAddCustomer: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onEditCustomer: (SyncItemEntity) -> Unit,
    onDeleteCustomer: (SyncItemEntity) -> Unit,
    onToggleSelection: (SyncItemEntity) -> Unit,
    onStartSelection: (SyncItemEntity) -> Unit
) {
    if (items.isEmpty()) {
        EmptyState(
            title = "No Data Found",
            message = "Add your first customer and it will sync to Firebase.",
            onAction = onAddCustomer,
            actionText = "Add Customer"
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        PrimaryButton(
            text = "Add Customer",
            onClick = onAddCustomer
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            items(items, key = { it.id }) { item ->
                SwipeableCustomerRow(
                    item = item,
                    onOpenDetails = onOpenDetails,
                    onEdit = { onEditCustomer(item) },
                    onDelete = { onDeleteCustomer(item) },
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedCustomerIds.contains(item.id),
                    onToggleSelection = { onToggleSelection(item) },
                    onStartSelection = { onStartSelection(item) }
                )
            }
        }
    }
}
