package com.app.softec.ui.screens.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StandardScaffold
import com.app.softec.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
    invoiceId: String?,
    selectedCustomerId: String?,
    onNavigateBack: () -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.editorState.collectAsState()
    val editorError = state.errorMessage
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(invoiceId, selectedCustomerId) {
        viewModel.prepareEditor(invoiceId, selectedCustomerId)
    }

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            viewModel.clearEditorCompletion()
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::updateEditorDueDate)
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    StandardScaffold(
        title = if (state.isEditMode) "Edit Invoice" else "Add Invoice",
        showBackButton = true,
        onNavigateBack = onNavigateBack
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (state.isEditMode) {
                        "Update invoice details and keep collections data current."
                    } else {
                        "Create a new invoice for the selected customer."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Customer: ${state.customerName.ifBlank { "Unknown Customer" }}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (state.contactNumber.isNotBlank()) {
                    Text(
                        text = "Contact: ${state.contactNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.totalAmountInput,
                    onValueChange = viewModel::updateEditorTotalAmount,
                    label = { Text("Total Amount Due") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.amountPaidInput,
                    onValueChange = viewModel::updateEditorAmountPaid,
                    label = { Text("Amount Paid") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        Text(
                            text = "Due Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.dueDateMillis.toBusinessDate(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Button(onClick = { showDatePicker = true }) {
                        Text("Change Date")
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.notes,
                    onValueChange = viewModel::updateEditorNotes,
                    label = { Text("Notes") },
                    placeholder = { Text("Optional payment context") },
                    minLines = 3
                )

                if (editorError != null) {
                    Text(
                        text = editorError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryButton(
                    text = if (state.isSaving) {
                        "Saving..."
                    } else if (state.isEditMode) {
                        "Update Invoice"
                    } else {
                        "Create Invoice"
                    },
                    onClick = viewModel::saveEditorInvoice,
                    enabled = !state.isSaving
                )
            }
        }
    }
}

private fun Long.toBusinessDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}
