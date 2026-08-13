package com.btween.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.R
import com.btween.app.ui.components.PasswordTextField
import com.btween.app.ui.theme.QuoteSerifFontFamily

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.didSucceed) {
        if (state.didSucceed) onRegisterSuccess()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (!state.awaitingRegistrationCode) {
                Text(
                    text = stringResource(R.string.auth_create_account_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = QuoteSerifFontFamily)
                )
                Text(
                    text = stringResource(R.string.auth_create_account_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = viewModel::onDisplayNameChanged,
                    label = { Text(stringResource(R.string.auth_label_display_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.username,
                    onValueChange = viewModel::onUsernameChanged,
                    label = { Text(stringResource(R.string.auth_label_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text(stringResource(R.string.auth_username_supporting)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text(stringResource(R.string.auth_label_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = stringResource(R.string.auth_label_password),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = stringResource(R.string.auth_password_supporting)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::onRegister,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading &&
                        state.displayName.isNotBlank() &&
                        state.username.isNotBlank() &&
                        state.email.isNotBlank() &&
                        state.password.length >= 8
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.auth_sign_up))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onNavigateToLogin) {
                        Text(stringResource(R.string.auth_have_account_log_in))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.auth_verify_registration_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = QuoteSerifFontFamily)
                )
                Text(
                    text = stringResource(R.string.auth_verify_registration_subtitle, state.email),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.registrationCode,
                    onValueChange = viewModel::onRegistrationCodeChanged,
                    label = { Text(stringResource(R.string.auth_label_code)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = viewModel::onCompleteRegistration,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isVerifyingRegistration && state.registrationCode.isNotBlank()
                ) {
                    if (state.isVerifyingRegistration) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.auth_verify_registration_action))
                    }
                }
            }
        }
    }
}
