package com.btween.app.domain.model

data class QuoteCollection(
    val id: Long,
    val name: String,
    val quoteCount: Int,
    val coverImageUrl: String? = null,
    val createdAt: String
)
