package com.btween.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.AppLanguage
import com.btween.app.domain.model.ThemeMode
import com.btween.app.domain.model.UserSettings
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.SettingsRepository
import com.btween.app.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationUiState(
    val isEmailVerified: Boolean = true,
    val showVerifyDialog: Boolean = false,
    val code: String = "",
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val infoMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _appLanguage = MutableStateFlow(LocaleManager.currentLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount

    private val _deleteAccountError = MutableStateFlow<String?>(null)
    val deleteAccountError: StateFlow<String?> = _deleteAccountError

    private val _emailVerification = MutableStateFlow(EmailVerificationUiState())
    val emailVerification: StateFlow<EmailVerificationUiState> = _emailVerification

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                profileRepository.getUser(userId).onSuccess { user ->
                    _emailVerification.value = _emailVerification.value.copy(isEmailVerified = user.emailVerified)
                }
            }
        }
    }

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onDynamicColorToggled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseDynamicColor(enabled) }
    }

    fun onLanguageSelected(language: AppLanguage) {
        _appLanguage.value = language
        LocaleManager.applyLanguage(language)
    }

    fun onLogout() {
        authRepository.logout()
    }

    fun consumeDeleteAccountError() {
        _deleteAccountError.value = null
    }

    /** On success, the caller's onDeleted callback should navigate back to the login gate -
     * logout() clears the session so AuthGate takes over automatically either way. */
    fun onDeleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isDeletingAccount.value = true
            profileRepository.deleteAccount()
                .onSuccess {
                    _isDeletingAccount.value = false
                    authRepository.logout()
                    onDeleted()
                }
                .onFailure { error ->
                    _isDeletingAccount.value = false
                    _deleteAccountError.value = error.message
                }
        }
    }

    fun onShowVerifyDialog() {
        _emailVerification.value = _emailVerification.value.copy(showVerifyDialog = true, code = "")
        onResendCode()
    }

    fun onDismissVerifyDialog() {
        _emailVerification.value = _emailVerification.value.copy(showVerifyDialog = false)
    }

    fun onCodeChanged(value: String) {
        _emailVerification.value = _emailVerification.value.copy(code = value)
    }

    fun consumeInfoMessage() {
        _emailVerification.value = _emailVerification.value.copy(infoMessage = null)
    }

    fun onResendCode() {
        val email = authRepository.getCurrentEmail() ?: return
        viewModelScope.launch {
            _emailVerification.value = _emailVerification.value.copy(isResending = true)
            authRepository.resendVerification(email)
                .onSuccess {
                    _emailVerification.value = _emailVerification.value.copy(
                        isResending = false,
                        infoMessage = "A new code has been sent to $email"
                    )
                }
                .onFailure { error ->
                    _emailVerification.value = _emailVerification.value.copy(
                        isResending = false,
                        infoMessage = error.message
                    )
                }
        }
    }

    fun onSubmitCode() {
        val email = authRepository.getCurrentEmail() ?: return
        val code = _emailVerification.value.code.trim()
        if (code.isEmpty()) return

        viewModelScope.launch {
            _emailVerification.value = _emailVerification.value.copy(isSubmitting = true)
            authRepository.verifyEmail(email, code)
                .onSuccess {
                    _emailVerification.value = _emailVerification.value.copy(
                        isSubmitting = false,
                        showVerifyDialog = false,
                        isEmailVerified = true,
                        infoMessage = "Email verified"
                    )
                }
                .onFailure { error ->
                    _emailVerification.value = _emailVerification.value.copy(
                        isSubmitting = false,
                        infoMessage = error.message
                    )
                }
        }
    }

    fun getCurrentUserId(): Long? = authRepository.getCurrentUserId()
}
