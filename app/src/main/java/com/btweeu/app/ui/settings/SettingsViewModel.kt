package com.btweeu.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.AppLanguage
import com.btweeu.app.domain.model.ThemeMode
import com.btweeu.app.domain.model.UserSettings
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.ProfileRepository
import com.btweeu.app.domain.repository.SettingsRepository
import com.btweeu.app.push.DeviceTokenRepository
import com.btweeu.app.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _appLanguage = MutableStateFlow(LocaleManager.currentLanguage(appContext))
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount

    private val _deleteAccountError = MutableStateFlow<String?>(null)
    val deleteAccountError: StateFlow<String?> = _deleteAccountError

    private val _emailVerification = MutableStateFlow(EmailVerificationUiState())
    val emailVerification: StateFlow<EmailVerificationUiState> = _emailVerification

    // null while still loading; true for local email/password accounts, false for
    // Google/Facebook/Microsoft accounts that never set a password. Used to skip requiring a
    // password confirmation when deleting an OAuth-only account, since there isn't one.
    private val _hasPassword = MutableStateFlow<Boolean?>(null)
    val hasPassword: StateFlow<Boolean?> = _hasPassword

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                profileRepository.getUser(userId).onSuccess { user ->
                    _emailVerification.value = _emailVerification.value.copy(isEmailVerified = user.emailVerified)
                    _hasPassword.value = user.authProvider == null
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
        LocaleManager.applyLanguage(appContext, language)
    }

    fun onLogout(onDone: () -> Unit) {
        viewModelScope.launch {
            deviceTokenRepository.unregisterCurrentToken()
            authRepository.logout()
            onDone()
        }
    }

    fun consumeDeleteAccountError() {
        _deleteAccountError.value = null
    }

    /** On success, the caller's onDeleted callback should navigate back to the login gate -
     * logout() clears the session so AuthGate takes over automatically either way. */
    fun onDeleteAccount(password: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isDeletingAccount.value = true
            profileRepository.deleteAccount(password)
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
        viewModelScope.launch {
            _emailVerification.value = _emailVerification.value.copy(isResending = true)
            authRepository.resendVerification()
                .onSuccess {
                    _emailVerification.value = _emailVerification.value.copy(
                        isResending = false,
                        infoMessage = "A new code has been sent"
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
        val code = _emailVerification.value.code.trim()
        if (code.isEmpty()) return

        viewModelScope.launch {
            _emailVerification.value = _emailVerification.value.copy(isSubmitting = true)
            authRepository.verifyEmail(code)
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
