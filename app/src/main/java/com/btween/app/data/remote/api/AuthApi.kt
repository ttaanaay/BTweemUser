package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.AuthResponseDto
import com.btween.app.data.remote.dto.ChangePasswordRequestDto
import com.btween.app.data.remote.dto.ForgotPasswordRequestDto
import com.btween.app.data.remote.dto.LoginRequestDto
import com.btween.app.data.remote.dto.MessageResponseDto
import com.btween.app.data.remote.dto.OAuthLoginRequestDto
import com.btween.app.data.remote.dto.RefreshRequestDto
import com.btween.app.data.remote.dto.RegisterRequestDto
import com.btween.app.data.remote.dto.RegistrationPendingResponseDto
import com.btween.app.data.remote.dto.ResendVerificationRequestDto
import com.btween.app.data.remote.dto.ResetPasswordRequestDto
import com.btween.app.data.remote.dto.VerifyCodeRequestDto
import com.btween.app.data.remote.dto.VerifyEmailRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegistrationPendingResponseDto

    @POST("auth/verify-registration")
    suspend fun verifyRegistration(@Body request: VerifyEmailRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): AuthResponseDto

    @POST("auth/oauth/google")
    suspend fun loginWithGoogle(@Body request: OAuthLoginRequestDto): AuthResponseDto

    @POST("auth/oauth/facebook")
    suspend fun loginWithFacebook(@Body request: OAuthLoginRequestDto): AuthResponseDto

    @POST("auth/oauth/microsoft")
    suspend fun loginWithMicrosoft(@Body request: OAuthLoginRequestDto): AuthResponseDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): MessageResponseDto

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): MessageResponseDto

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequestDto): MessageResponseDto

    // Session-based counterparts - the server already knows which account to verify from
    // the auth token, so there's no email to pass (and no way to accidentally target a
    // different account by relying on a possibly-stale cached email).
    @POST("auth/verify-email-me")
    suspend fun verifyEmailMe(@Body request: VerifyCodeRequestDto): MessageResponseDto

    @POST("auth/resend-verification-me")
    suspend fun resendVerificationMe(): MessageResponseDto

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto): MessageResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequestDto): MessageResponseDto
}
