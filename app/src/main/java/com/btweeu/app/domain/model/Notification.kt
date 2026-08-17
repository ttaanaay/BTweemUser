package com.btweeu.app.domain.model

data class Notification(
    val id: Long,
    val type: String,
    val actorId: Long,
    val actorUsername: String,
    val actorDisplayName: String,
    val quoteId: Long?,
    val quoteTextPreview: String?,
    val isRead: Boolean,
    val createdAt: String
)
