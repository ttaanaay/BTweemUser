package com.btweeu.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val codeRequested: Boolean = false,
    val didSucceed: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChanged(value: String) = update { it.copy(email = value) }
    fun onCodeChanged(value: String) = update { it.copy(code = value) }
    fun onNewPasswordChanged(value: String) = update { it.copy(newPassword = value) }
    fun consumeError() = update { it.copy(errorMessage = null) }
    fun consumeInfo() = update { it.copy(infoMessage = null) }

    private inline fun update(block: (ForgotPasswordUiState) -> ForgotPasswordUiState) {
        _uiState.value = block(_uiState.value)
    }

    fun onRequestCode() {
        val email = _uiState.value.email.trim()
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.forgotPassword(email)
                .onSuccess {
                    update {
                        it.copy(
                            isLoading = false,
                            codeRequested = true,
                            infoMessage = "If that email has an account, a reset code has been sent."
                        )
                    }
                }
                .onFailure { error ->
                    update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onResetPassword() {
        val state = _uiState.value
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.resetPassword(state.email.trim(), state.code.trim(), state.newPassword)
                .onSuccess {
                    update { it.copy(isLoading = false, didSucceed = true) }
                }
                .onFailure { error ->
                    update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
