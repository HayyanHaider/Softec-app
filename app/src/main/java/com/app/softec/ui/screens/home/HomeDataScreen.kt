package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var showAddCustomerDialog by rememberSaveable { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<SyncItemEntity?>(null) }
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

    customerToEdit?.let { item ->
        val (phone, email) = parseCustomerNotes(item.notes)
        AddCustomerDialog(
            title = "Edit Customer",
            initialName = item.title,
            initialPhone = phone,
            initialEmail = email,
            submitLabel = "Update",
            onDismiss = { customerToEdit = null },
            onSubmit = { name, updatedPhone, updatedEmail ->
                viewModel.updateCustomer(
                    item = item,
                    name = name,
                    phone = updatedPhone,
                    email = updatedEmail
                )
                customerToEdit = null
            }
        )
    }

    customerToDelete?.let { item ->
        DeleteCustomerDialog(
            onDismiss = { customerToDelete = null },
            onConfirm = {
                viewModel.deleteCustomer(item)
                customerToDelete = null
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
                        SwipeableCustomerRow(
                            item = item,
                            onOpenDetails = onOpenDetails,
                            onEdit = { customerToEdit = item },
                            onDelete = { customerToDelete = item }
                        )
                    }
                }
            }
        }
    }
}