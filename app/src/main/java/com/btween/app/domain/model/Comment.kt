package com.btween.app.domain.model

data class Comment(
    val id: Long,
    val quoteId: Long,
    val text: String,
    val author: User,
    val createdAt: String
)
