package com.btween.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequestDto(
    val targetType: String,
    val targetId: Long,
    val reason: String,
    val details: String? = null
)
