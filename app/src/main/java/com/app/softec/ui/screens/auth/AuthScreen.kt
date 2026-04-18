package com.app.softec.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.app.softec.R
import com.app.softec.core.ui.components.PrimaryButton
import com.app.softec.core.ui.components.StyledTextField
import com.app.softec.ui.theme.spacing
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

private enum class AuthMode {
    SignIn,
    SignUp
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()

    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val passwordVisual = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = MaterialTheme.spacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Text(
            text = if (mode == AuthMode.SignIn) "Welcome back" else "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )
        Text(
            text = "Sign in with email/password or continue with Google.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        StyledTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            label = "Email",
            placeholder = "you@example.com",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        StyledTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = "Password",
            visualTransformation = passwordVisual,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (mode == AuthMode.SignIn) ImeAction.Done else ImeAction.Next
            ),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            }
        )

        if (mode == AuthMode.SignUp) {
            StyledTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    viewModel.clearError()
                },
                label = "Confirm password",
                visualTransformation = passwordVisual,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )
        }

        PrimaryButton(
            text = when {
                uiState.isLoading && mode == AuthMode.SignIn -> "Signing in..."
                uiState.isLoading && mode == AuthMode.SignUp -> "Creating account..."
                mode == AuthMode.SignIn -> "Sign in"
                else -> "Create account"
            },
            onClick = {
                if (mode == AuthMode.SignIn) {
                    viewModel.signInWithEmail(email, password)
                } else {
                    viewModel.signUpWithEmail(email, password, confirmPassword)
                }
            },
            enabled = !uiState.isLoading
        )

        TextButton(
            onClick = {
                mode = if (mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn
                viewModel.clearError()
            },
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(
                text = if (mode == AuthMode.SignIn) {
                    "Need an account? Sign up"
                } else {
                    "Already have an account? Sign in"
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium))

        OutlinedButton(
            onClick = {
                scope.launch {
                    val idToken = runCatching {
                        requestGoogleIdToken(
                            credentialManager = credentialManager,
                            serverClientId = context.getString(R.string.default_web_client_id),
                            context = context
                        )
                    }.getOrElse { throwable ->
                        viewModel.reportExternalError(
                            throwable.message ?: "Google sign-in was cancelled"
                        )
                        return@launch
                    }
                    viewModel.signInWithGoogleIdToken(idToken)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text("Continue with Google")
        }
    }
}

private suspend fun requestGoogleIdToken(
    credentialManager: CredentialManager,
    serverClientId: String,
    context: android.content.Context
): String {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(serverClientId)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val credentialResponse = credentialManager.getCredential(
        context = context,
        request = request
    )
    val credential = credentialResponse.credential

    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Failed to parse Google ID token", e)
        }
    }

    throw IllegalStateException("Google account credential was not returned")
}