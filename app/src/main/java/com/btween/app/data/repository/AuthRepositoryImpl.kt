package com.btween.app.data.repository

import com.btween.app.data.remote.TokenManager
import com.btween.app.data.remote.api.AuthApi
import com.btween.app.data.remote.dto.ForgotPasswordRequestDto
import com.btween.app.data.remote.dto.LoginRequestDto
import com.btween.app.data.remote.dto.OAuthLoginRequestDto
import com.btween.app.data.remote.dto.RegisterRequestDto
import com.btween.app.data.remote.dto.ResendVerificationRequestDto
import com.btween.app.data.remote.dto.ResetPasswordRequestDto
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
    ): Result<User> = safeApiCall {
        val response = authApi.register(RegisterRequestDto(username, email, password, displayName))
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

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> = safeApiCall {
        authApi.verifyEmail(VerifyEmailRequestDto(email, code))
        Unit
    }

    override suspend fun resendVerification(email: String): Result<Unit> = safeApiCall {
        authApi.resendVerification(ResendVerificationRequestDto(email))
        Unit
    }

    override fun logout() {
        tokenManager.clearSession()
    }

    override fun getCurrentUserId(): Long? = tokenManager.getUserId()

    override fun getCurrentEmail(): String? = tokenManager.getEmail()
}
