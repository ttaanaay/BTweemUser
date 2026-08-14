package com.btween.app.domain.repository

import com.btween.app.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val isLoggedIn: StateFlow<Boolean>

    /** Creates the account and sends a verification code - doesn't log the person in yet.
     * Call [completeRegistration] with the code to actually finish signing up. */
    suspend fun register(username: String, email: String, password: String, displayName: String): Result<Unit>

    /** Verifies the code sent by [register] and, on success, logs the person in for the
     * first time (this is the point a new account actually becomes usable). */
    suspend fun completeRegistration(email: String, code: String): Result<User>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun loginWithGoogle(idToken: String): Result<User>

    suspend fun loginWithFacebook(accessToken: String): Result<User>

    suspend fun loginWithMicrosoft(accessToken: String): Result<User>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>

    suspend fun verifyEmail(code: String): Result<Unit>

    suspend fun resendVerification(): Result<Unit>

    /** Email-based variant for when there's no session yet - used when someone tries to log
     * in before verifying and needs a fresh code without being authenticated first. */
    suspend fun resendVerificationForEmail(email: String): Result<Unit>

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun logout()

    fun getCurrentUserId(): Long?

    fun getCurrentEmail(): String?

    fun isOnboardingPending(): Boolean

    fun onOnboardingCompleted()
}
