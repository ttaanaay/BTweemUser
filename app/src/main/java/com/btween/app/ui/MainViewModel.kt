package com.btween.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.ThemeMode
import com.btween.app.domain.model.UserSettings
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.MaintenanceRepository
import com.btween.app.domain.repository.MaintenanceStatus
import com.btween.app.domain.repository.SettingsRepository
import com.btween.app.push.DeviceTokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val maintenanceRepository: MaintenanceRepository,
    deviceTokenRepository: DeviceTokenRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSettings()
        )

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    // Reads the pending-onboarding flag whenever the login state changes - covers both a
    // fresh registration (flag was just set) and returning to an existing session (flag is
    // false, so this stays false and onboarding never re-shows).
    private val _shouldShowOnboarding = MutableStateFlow(authRepository.isOnboardingPending())
    val shouldShowOnboarding: StateFlow<Boolean> = _shouldShowOnboarding

    fun onOnboardingCompleted() {
        authRepository.onOnboardingCompleted()
        _shouldShowOnboarding.value = false
    }

    // Null while unchecked (renders nothing, avoiding a flash of the real app before a
    // maintenance screen would replace it) or if the check itself failed - a failed check
    // fails open (shows the app) rather than risk locking everyone out over a network hiccup.
    private val _maintenanceStatus = MutableStateFlow<MaintenanceStatus?>(null)
    val maintenanceStatus: StateFlow<MaintenanceStatus?> = _maintenanceStatus

    init {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                _shouldShowOnboarding.value = loggedIn && authRepository.isOnboardingPending()
            }
        }

        // Covers the "already logged in, app was just reopened" case - login/register cover
        // the fresh-sign-in case themselves right after succeeding.
        if (authRepository.getCurrentUserId() != null) {
            viewModelScope.launch { deviceTokenRepository.registerCurrentToken() }
        }

        viewModelScope.launch {
            maintenanceRepository.getStatus()
                .onSuccess { _maintenanceStatus.value = it }
                .onFailure { _maintenanceStatus.value = MaintenanceStatus(enabled = false, message = null) }
        }
    }
}

/**
 * Resolves the user's saved [ThemeMode] preference against the current system setting to a
 * concrete dark/light boolean, for use as `BTweenTheme(darkTheme = ...)`.
 */
fun ThemeMode.resolveIsDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
}
