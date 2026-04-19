package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.ui.theme.spacing

@Composable
fun SwipeableCustomerRow(
    item: SyncItemEntity,
    onOpenDetails: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> onEdit()
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit customer",
                    tint = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete customer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        SyncItemCard(item = item, onOpenDetails = onOpenDetails)
    }
}