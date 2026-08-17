package com.btweeu.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.ui.components.PasswordTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.didSucceed) {
        if (state.didSucceed) onBack()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_password_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PasswordTextField(
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChanged,
                label = stringResource(R.string.change_password_label_current),
                modifier = Modifier.fillMaxWidth()
            )
            PasswordTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                label = stringResource(R.string.auth_label_new_password),
                modifier = Modifier.fillMaxWidth(),
                supportingText = stringResource(R.string.auth_password_supporting)
            )
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = stringResource(R.string.change_password_label_confirm),
                modifier = Modifier.fillMaxWidth(),
                supportingText = if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword) {
                    stringResource(R.string.change_password_mismatch)
                } else {
                    null
                }
            )

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit && !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                } else {
                    Text(stringResource(R.string.change_password_action_update))
                }
            }
        }
    }
}
