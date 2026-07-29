package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.AuthResponseDto
import com.btween.app.data.remote.dto.ForgotPasswordRequestDto
import com.btween.app.data.remote.dto.LoginRequestDto
import com.btween.app.data.remote.dto.MessageResponseDto
import com.btween.app.data.remote.dto.OAuthLoginRequestDto
import com.btween.app.data.remote.dto.RefreshRequestDto
import com.btween.app.data.remote.dto.RegisterRequestDto
import com.btween.app.data.remote.dto.ResetPasswordRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

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
}
