package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var showAddCustomerDialog by rememberSaveable { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<SyncItemEntity?>(null) }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onSubmit = { name, phone, email ->
                viewModel.addCustomer(
                    name = name,
                    phone = phone,
                    email = email
                )
                showAddCustomerDialog = false
            }
        )
    }

    customerToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { androidx.compose.material3.Text("Delete Customer") },
            text = {
                androidx.compose.material3.Text(
                    "Are you sure that you want to delete the customer?"
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteCustomer(item)
                        customerToDelete = null
                    }
                ) {
                    androidx.compose.material3.Text("Delete")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { customerToDelete = null }
                ) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

    StatefulContent(
        state = state,
        modifier = modifier.fillMaxSize(),
        onRetry = viewModel::syncNow
    ) { items ->
        if (items.isEmpty()) {
            EmptyState(
                title = "No Data Found",
                message = "Add your first customer and it will sync to Firebase.",
                onAction = { showAddCustomerDialog = true },
                actionText = "Add Customer"
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                PrimaryButton(
                    text = "Add Customer",
                    onClick = { showAddCustomerDialog = true }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    items(items, key = { it.id }) { item ->
                        val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                    customerToDelete = item
                                }
                                false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = MaterialTheme.spacing.medium),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete customer",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        ) {
                            SyncItemCard(item = item, onOpenDetails = onOpenDetails)
                        }
                    }
                }
            }
        }
    }
}
