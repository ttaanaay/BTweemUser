package com.btweeu.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.push.DeviceTokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val didSucceed: Boolean = false,
    // Set once /auth/register has sent a code - the register screen switches to a code
    // entry step instead of navigating away, since the account isn't usable until
    // completeRegistration() succeeds.
    val awaitingRegistrationCode: Boolean = false,
    val registrationCode: String = "",
    val isVerifyingRegistration: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onUsernameChanged(value: String) = update { it.copy(username = value) }
    fun onDisplayNameChanged(value: String) = update { it.copy(displayName = value) }
    fun onEmailChanged(value: String) = update { it.copy(email = value) }
    fun onPasswordChanged(value: String) = update { it.copy(password = value) }
    fun consumeError() = update { it.copy(errorMessage = null) }

    private inline fun update(block: (AuthUiState) -> AuthUiState) {
        _uiState.value = block(_uiState.value)
    }

    /** Registers this device for push notifications right after a successful sign-in - no
     * need to wait for FCM's own token-rotation timing to eventually deliver it. */
    private fun registerDeviceForPush() {
        viewModelScope.launch { deviceTokenRepository.registerCurrentToken() }
    }

    fun onRegister() {
        val state = _uiState.value
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.register(
                username = state.username.trim(),
                email = state.email.trim(),
                password = state.password,
                displayName = state.displayName.trim()
            ).onSuccess {
                update { it.copy(isLoading = false, awaitingRegistrationCode = true) }
            }.onFailure { error ->
                update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun onRegistrationCodeChanged(value: String) = update { it.copy(registrationCode = value) }

    fun onResendLoginVerificationCode() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) return
        viewModelScope.launch {
            authRepository.resendVerificationForEmail(email)
                .onFailure { error -> update { it.copy(errorMessage = error.message) } }
        }
    }

    fun onCompleteRegistration() {
        val state = _uiState.value
        val code = state.registrationCode.trim()
        if (code.isEmpty()) return

        viewModelScope.launch {
            update { it.copy(isVerifyingRegistration = true) }
            authRepository.completeRegistration(state.email.trim(), code)
                .onSuccess {
                    update { it.copy(isVerifyingRegistration = false, didSucceed = true) }
                    registerDeviceForPush()
                }
                .onFailure { error ->
                    update { it.copy(isVerifyingRegistration = false, errorMessage = error.message) }
                }
        }
    }

    fun onLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.login(state.email.trim(), state.password)
                .onSuccess {
                    update { it.copy(isLoading = false, didSucceed = true) }
                    registerDeviceForPush()
                }
                .onFailure { error ->
                    // The account exists and the password is right, it just hasn't been
                    // verified yet - offer the same code-entry step used right after
                    // registration instead of leaving this as a dead-end error.
                    val needsVerification = error.message?.contains("verify your email", ignoreCase = true) == true
                    update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                            awaitingRegistrationCode = needsVerification
                        )
                    }
                }
        }
    }

    fun onGoogleSignInResult(idToken: String?, errorDetail: String?) {
        if (idToken == null) {
            update { it.copy(errorMessage = errorDetail ?: "Google sign-in was cancelled or failed") }
            return
        }
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.loginWithGoogle(idToken)
                .onSuccess {
                    update { it.copy(isLoading = false, didSucceed = true) }
                    registerDeviceForPush()
                }
                .onFailure { error -> update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun onFacebookLoginResult(accessToken: String?) {
        if (accessToken == null) {
            update { it.copy(errorMessage = "Facebook sign-in was cancelled or failed") }
            return
        }
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            authRepository.loginWithFacebook(accessToken)
                .onSuccess {
                    update { it.copy(isLoading = false, didSucceed = true) }
                    registerDeviceForPush()
                }
                .onFailure { error -> update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }
}
