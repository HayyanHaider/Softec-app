package com.app.softec.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.UiState
import com.app.softec.data.local.entity.SyncItemEntity
@Composable
fun HomeDataScreen(
    modifier: Modifier = Modifier,
    onOpenDetails: (String) -> Unit,
    onSelectionStateChange: (Boolean, Int) -> Unit,
    onRegisterDeleteSelectedAction: ((() -> Unit)?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddCustomerDialog by rememberSaveable { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<SyncItemEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<SyncItemEntity?>(null) }
    var showDeleteSelectedDialog by rememberSaveable { mutableStateOf(false) }
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedCustomerIds by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(isSelectionMode, selectedCustomerIds) {
        onSelectionStateChange(isSelectionMode, selectedCustomerIds.size)
        onRegisterDeleteSelectedAction(
            if (isSelectionMode) {
                { showDeleteSelectedDialog = true }
            } else {
                null
            }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            onSelectionStateChange(false, 0)
            onRegisterDeleteSelectedAction(null)
        }
    }
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
    if (showDeleteSelectedDialog) {
        DeleteSelectedCustomersDialog(
            selectedCount = selectedCustomerIds.size,
            onDismiss = { showDeleteSelectedDialog = false },
            onConfirm = {
                val selectedItems = (state as? UiState.Success<List<SyncItemEntity>>)
                    ?.data
                    ?.filter { selectedCustomerIds.contains(it.id) }
                    .orEmpty()
                viewModel.deleteCustomers(selectedItems)
                showDeleteSelectedDialog = false
                selectedCustomerIds = emptySet()
                isSelectionMode = false
            }
        )
    }
    HomeCustomerListContent(
        state = state,
        modifier = modifier,
        onRetry = viewModel::syncNow
    ) { items ->
        LaunchedEffect(items, selectedCustomerIds, isSelectionMode) {
            val validIds = items.map { it.id }.toSet()
            val filteredSelection = selectedCustomerIds.filter { validIds.contains(it) }.toSet()
            if (filteredSelection != selectedCustomerIds) {
                selectedCustomerIds = filteredSelection
            }
            if (isSelectionMode && filteredSelection.isEmpty()) {
                isSelectionMode = false
            }
        }
        val updateSelection: (SyncItemEntity) -> Unit = { item ->
            val updatedSelection = if (selectedCustomerIds.contains(item.id)) {
                selectedCustomerIds - item.id
            } else {
                selectedCustomerIds + item.id
            }
            selectedCustomerIds = updatedSelection
            if (updatedSelection.isEmpty()) {
                isSelectionMode = false
            }
        }
        val startSelection: (SyncItemEntity) -> Unit = { item ->
            isSelectionMode = true
            selectedCustomerIds = selectedCustomerIds + item.id
        }
        CustomerRowsContent(
            items = items,
            isSelectionMode = isSelectionMode,
            selectedCustomerIds = selectedCustomerIds,
            onAddCustomer = { showAddCustomerDialog = true },
            onOpenDetails = onOpenDetails,
            onEditCustomer = { customerToEdit = it },
            onDeleteCustomer = { customerToDelete = it },
            onToggleSelection = updateSelection,
            onStartSelection = startSelection
        )
    }
}