package com.btweeu.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequestDto(
    val text: String
)

@Serializable
data class CommentResponseDto(
    val id: Long,
    val quoteId: Long,
    val text: String,
    val author: UserResponseDto,
    val isEdited: Boolean = false,
    val createdAt: String
)
