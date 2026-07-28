package com.btween.app.domain.model

data class SocialQuote(
    val id: Long,
    val text: String,
    val sourceTitle: String,
    val sourceType: SourceType,
    val speaker: String,
    val author: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val visibility: String,
    val likeCount: Int,
    val isLikedByMe: Boolean,
    val owner: User,
    val createdAt: String
)
