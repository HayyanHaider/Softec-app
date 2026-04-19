package com.app.softec.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.ui.theme.spacing

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SyncItemCard(
    item: SyncItemEntity,
    onOpenDetails: (String) -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onStartSelection: () -> Unit
) {
    val customerInitials = getInitials(item.title)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else {
                        onOpenDetails(item.id)
                    }
                },
                onLongClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else {
                        onStartSelection()
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() }
                )
                Spacer(modifier = Modifier.size(MaterialTheme.spacing.small))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customerInitials,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.size(MaterialTheme.spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                if (!item.notes.isNullOrBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
                    )
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { onOpenDetails(item.id) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open customer details"
                    )
                }
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }

    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
