package com.btween.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String,
    val actorId: Long,
    val actorUsername: String,
    val actorDisplayName: String,
    val quoteId: Long? = null,
    val quoteTextPreview: String? = null,
    val isRead: Boolean,
    val createdAt: String
)

@Serializable
data class UnreadCountDto(val count: Long)
