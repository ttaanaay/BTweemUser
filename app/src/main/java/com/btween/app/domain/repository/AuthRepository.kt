package com.btween.app.domain.repository

import com.btween.app.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val isLoggedIn: StateFlow<Boolean>

    suspend fun register(username: String, email: String, password: String, displayName: String): Result<User>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun loginWithGoogle(idToken: String): Result<User>

    suspend fun loginWithFacebook(accessToken: String): Result<User>

    suspend fun loginWithMicrosoft(accessToken: String): Result<User>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>

    suspend fun verifyEmail(email: String, code: String): Result<Unit>

    suspend fun resendVerification(email: String): Result<Unit>

    fun logout()

    fun getCurrentUserId(): Long?

    fun getCurrentEmail(): String?

    fun isOnboardingPending(): Boolean

    fun onOnboardingCompleted()
}
