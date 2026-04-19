package com.app.softec.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCustomerDialog(
    title: String = "Add Customer",
    initialName: String = "",
    initialPhone: String = "",
    initialEmail: String = "",
    submitLabel: String = "Save",
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, email: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var phone by rememberSaveable { mutableStateOf(initialPhone) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val isNameInvalid = attemptedSubmit && name.isBlank()
    val isPhoneInvalid = phone.isNotBlank() && !isValidPhone(phone)
    val isEmailInvalid = email.isNotBlank() && !isValidEmail(email)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer name") },
                    singleLine = true,
                    isError = isNameInvalid
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = phone,
                    onValueChange = { phone = it.filterPhoneCharacters() },
                    label = { Text("Phone") },
                    singleLine = true,
                    isError = isPhoneInvalid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = {
                        if (isPhoneInvalid) {
                            Text("Enter a valid phone number")
                        }
                    }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = isEmailInvalid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    supportingText = {
                        if (isEmailInvalid) {
                            Text("Enter a valid email address")
                        }
                    }
                )
                if (isNameInvalid) {
                    Text(
                        text = "Customer name is required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attemptedSubmit = true
                    if (name.isBlank() || isPhoneInvalid || isEmailInvalid) {
                        return@TextButton
                    }
                    onSubmit(name.trim(), phone.trim(), email.trim())
                }
            ) {
                Text(submitLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun String.filterPhoneCharacters(): String {
    return filter { char ->
        char.isDigit() || char == '+' || char == '-' || char == ' ' || char == '(' || char == ')'
    }
}

private fun isValidPhone(phone: String): Boolean {
    val digitsOnly = phone.filter { it.isDigit() }
    return digitsOnly.length in 7..15
}

private fun isValidEmail(email: String): Boolean {
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email)
}