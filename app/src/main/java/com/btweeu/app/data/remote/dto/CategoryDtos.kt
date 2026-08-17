package com.btweeu.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponseDto(
    val id: Long,
    val name: String,
    val icon: String
)
