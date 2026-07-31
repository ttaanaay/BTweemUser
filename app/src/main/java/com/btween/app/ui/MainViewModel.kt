package com.btween.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.ThemeMode
import com.btween.app.domain.model.UserSettings
import com.btween.app.domain.repository.AuthRepository
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
