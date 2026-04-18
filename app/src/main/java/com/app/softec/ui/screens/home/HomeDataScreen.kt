package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.EmptyState
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StatefulContent
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.ui.theme.spacing

@Composable
fun HomeDataScreen(
    modifier: Modifier = Modifier,
    onOpenDetails: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    StatefulContent(
        state = state,
        modifier = modifier.fillMaxSize(),
        onRetry = viewModel::syncNow
    ) { items ->
        if (items.isEmpty()) {
            EmptyState(
                title = "No Data Found",
                message = "Create your first local item and it will sync to Firebase.",
                onAction = viewModel::addSampleItem,
                actionText = "Create Sample"
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                PrimaryButton(
                    text = "Add Sample Item",
                    onClick = viewModel::addSampleItem
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    items(items, key = { it.id }) { item ->
                        SyncItemCard(item = item, onOpenDetails = onOpenDetails)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncItemCard(
    item: SyncItemEntity,
    onOpenDetails: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { onOpenDetails(item.id) }
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            if (!item.notes.isNullOrBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
                )
            }
            if (item.tags.isNotEmpty()) {
                Text(
                    text = item.tags.joinToString(prefix = "#", separator = " #"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small)
                )
            }
        }
    }
}
