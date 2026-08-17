package com.btweeu.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSaving: Boolean = false,
    val didSucceed: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() &&
            newPassword.length >= 8 &&
            newPassword == confirmPassword
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState

    fun onCurrentPasswordChanged(value: String) = update { it.copy(currentPassword = value) }
    fun onNewPasswordChanged(value: String) = update { it.copy(newPassword = value) }
    fun onConfirmPasswordChanged(value: String) = update { it.copy(confirmPassword = value) }
    fun consumeError() = update { it.copy(errorMessage = null) }

    private inline fun update(block: (ChangePasswordUiState) -> ChangePasswordUiState) {
        _uiState.value = block(_uiState.value)
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            authRepository.changePassword(state.currentPassword, state.newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, didSucceed = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                }
        }
    }
}
