package com.btween.app.domain.repository

import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.SourceType

interface SocialQuoteRepository {

    suspend fun getFeed(limit: Int = 20, offset: Long = 0, scope: String = "recommended"): Result<List<SocialQuote>>

    suspend fun getQuote(id: Long): Result<SocialQuote>

    suspend fun getUserQuotes(userId: Long, limit: Int = 20, offset: Long = 0): Result<List<SocialQuote>>

    suspend fun createQuote(
        text: String,
        sourceTitle: String,
        sourceType: SourceType,
        speaker: String,
        author: String?,
        category: String?,
        tags: List<String>,
        imageUrl: String?,
        visibility: String
    ): Result<SocialQuote>

    suspend fun updateQuote(
        id: Long,
        text: String,
        sourceTitle: String,
        sourceType: SourceType,
        speaker: String,
        author: String?,
        category: String?,
        tags: List<String>,
        imageUrl: String?,
        visibility: String
    ): Result<SocialQuote>

    suspend fun deleteQuote(id: Long): Result<Unit>

    suspend fun likeQuote(id: Long): Result<Unit>

    suspend fun unlikeQuote(id: Long): Result<Unit>
}
