package com.btween.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SourceTypeResponseDto(
    val id: Long,
    val name: String
)
