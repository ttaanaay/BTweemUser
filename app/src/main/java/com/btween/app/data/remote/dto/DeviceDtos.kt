package com.btween.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequestDto(
    val fcmToken: String
)
