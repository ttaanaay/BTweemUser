package com.btweeu.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the access/refresh tokens and the signed-in user's id using
 * EncryptedSharedPreferences (AES-256, backed by the Android Keystore) - never plain
 * SharedPreferences, since these are long-lived credentials.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "btweeu_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _isLoggedIn = MutableStateFlow(hasStoredSession())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private fun hasStoredSession(): Boolean = prefs.getString(KEY_ACCESS_TOKEN, null) != null

    fun saveSession(accessToken: String, refreshToken: String, userId: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_USER_ID, userId)
            .apply()
        _isLoggedIn.value = true
    }

    /**
     * The server's public UserResponse never includes email (other users shouldn't see it),
     * so there's no way to fetch it back from the API for the signed-in user's own Settings
     * screen. Persisting it locally at register/login time (email/password path only - the
     * OAuth paths never have a locally-typed email, but those accounts start out
     * pre-verified anyway so this is never needed for them) avoids needing a new endpoint.
     */
    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /** Set right after a fresh registration succeeds, so the app shows the one-time
     * onboarding flow (pick interests, follow suggestions) before landing on Home. Existing
     * users logging in normally never have this set, so they skip straight to the app. */
    fun setOnboardingPending(pending: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_PENDING, pending).apply()
    }

    fun isOnboardingPending(): Boolean = prefs.getBoolean(KEY_ONBOARDING_PENDING, false)

    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): Long? = prefs.getLong(KEY_USER_ID, -1L).takeIf { it != -1L }

    fun clearSession() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        const val KEY_ONBOARDING_PENDING = "onboarding_pending"
    }
}
