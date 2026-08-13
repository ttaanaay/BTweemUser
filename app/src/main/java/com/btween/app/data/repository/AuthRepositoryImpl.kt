package com.btween.app.data.repository

import com.btween.app.data.remote.TokenManager
import com.btween.app.data.remote.api.AuthApi
import com.btween.app.data.remote.dto.ChangePasswordRequestDto
import com.btween.app.data.remote.dto.ForgotPasswordRequestDto
import com.btween.app.data.remote.dto.LoginRequestDto
import com.btween.app.data.remote.dto.OAuthLoginRequestDto
import com.btween.app.data.remote.dto.RefreshRequestDto
import com.btween.app.data.remote.dto.RegisterRequestDto
import com.btween.app.data.remote.dto.ResetPasswordRequestDto
import com.btween.app.data.remote.dto.VerifyCodeRequestDto
import com.btween.app.data.remote.dto.VerifyEmailRequestDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.User
import com.btween.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override val isLoggedIn: StateFlow<Boolean> = tokenManager.isLoggedIn

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Result<Unit> = safeApiCall {
        authApi.register(RegisterRequestDto(username, email, password, displayName))
        Unit
    }

    override suspend fun completeRegistration(email: String, code: String): Result<User> = safeApiCall {
        val response = authApi.verifyRegistration(VerifyEmailRequestDto(email, code))
        // Must be set before saveSession(): saveSession() flips isLoggedIn to true, which
        // MainViewModel reacts to immediately by checking this same flag - if it ran after,
        // that check would race and read the stale "false" value.
        tokenManager.setOnboardingPending(true)
        tokenManager.saveSession(response.accessToken, response.refreshToken, response.user.id)
        tokenManager.saveEmail(email)
        response.user.toDomain()
    }

    override suspend fun login(email: String, password: String): Result<User> = safeApiCall {
        val response = authApi.login(LoginRequestDto(email, password))
        tokenManager.saveSession(response.accessToken, response.refreshToken, response.user.id)
        tokenManager.saveEmail(email)
        response.user.toDomain()
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> = safeApiCall {
        val response = authApi.loginWithGoogle(OAuthLoginRequestDto(idToken))
        tokenManager.saveSession(response.accessToken, response.refreshToken, response.user.id)
        response.user.toDomain()
    }

    override suspend fun loginWithFacebook(accessToken: String): Result<User> = safeApiCall {
        val response = authApi.loginWithFacebook(OAuthLoginRequestDto(accessToken))
        tokenManager.saveSession(response.accessToken, response.refreshToken, response.user.id)
        response.user.toDomain()
    }

    override suspend fun loginWithMicrosoft(accessToken: String): Result<User> = safeApiCall {
        val response = authApi.loginWithMicrosoft(OAuthLoginRequestDto(accessToken))
        tokenManager.saveSession(response.accessToken, response.refreshToken, response.user.id)
        response.user.toDomain()
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = safeApiCall {
        authApi.forgotPassword(ForgotPasswordRequestDto(email))
        Unit
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> = safeApiCall {
        authApi.resetPassword(ResetPasswordRequestDto(email, code, newPassword))
        Unit
    }

    override suspend fun verifyEmail(code: String): Result<Unit> = safeApiCall {
        authApi.verifyEmailMe(VerifyCodeRequestDto(code))
        Unit
    }

    override suspend fun resendVerification(): Result<Unit> = safeApiCall {
        authApi.resendVerificationMe()
        Unit
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = safeApiCall {
        authApi.changePassword(ChangePasswordRequestDto(currentPassword, newPassword))
        Unit
    }

    override suspend fun logout() {
        // Best-effort: revoke the refresh token server-side so it can't be replayed even if
        // it leaked. If this fails (offline, server down, token already invalid) that's
        // fine - clearing the local session below still logs the user out on this device
        // either way, and an already-expired/invalid token has nothing useful left to revoke.
        try {
            tokenManager.getRefreshToken()?.let { refreshToken ->
                authApi.logout(RefreshRequestDto(refreshToken))
            }
        } catch (e: Exception) {
            // Ignored - see comment above.
        }
        tokenManager.clearSession()
    }

    override fun getCurrentUserId(): Long? = tokenManager.getUserId()

    override fun getCurrentEmail(): String? = tokenManager.getEmail()

    override fun isOnboardingPending(): Boolean = tokenManager.isOnboardingPending()

    override fun onOnboardingCompleted() {
        tokenManager.setOnboardingPending(false)
    }
}
