package com.btweeu.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionRequestDto(
    val name: String
)

@Serializable
data class AddItemRequestDto(
    val quoteId: Long
)

@Serializable
data class CollectionResponseDto(
    val id: Long,
    val name: String,
    val quoteCount: Int,
    val coverImageUrl: String? = null,
    val createdAt: String
)

@Serializable
data class CollectionDetailResponseDto(
    val id: Long,
    val name: String,
    val quotes: List<QuoteResponseDto>,
    val createdAt: String
)
