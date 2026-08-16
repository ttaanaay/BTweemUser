package com.btween.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceStatusResponseDto(
    val enabled: Boolean,
    val message: String? = null
)
