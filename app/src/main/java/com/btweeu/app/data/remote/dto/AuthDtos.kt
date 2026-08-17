package com.btweeu.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)

@Serializable
data class OAuthLoginRequestDto(
    val token: String
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String
)

@Serializable
data class ResetPasswordRequestDto(
    val email: String,
    val code: String,
    val newPassword: String
)

@Serializable
data class VerifyEmailRequestDto(
    val email: String,
    val code: String
)

@Serializable
data class VerifyCodeRequestDto(
    val code: String
)

@Serializable
data class ResendVerificationRequestDto(
    val email: String
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class MessageResponseDto(
    val message: String
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto
)

@Serializable
data class RegistrationPendingResponseDto(
    val email: String,
    val message: String
)

@Serializable
data class UserResponseDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByMe: Boolean = false,
    val emailVerified: Boolean = true,
    val authProvider: String? = null
)

@Serializable
data class TopContributorResponseDto(
    val user: UserResponseDto,
    val quoteCount: Int
)

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null
)

@Serializable
data class DeleteAccountRequestDto(
    val password: String
)

@Serializable
data class ErrorResponseDto(
    val message: String
)
