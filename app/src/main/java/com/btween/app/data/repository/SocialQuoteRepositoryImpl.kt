package com.btween.app.data.repository

import com.btween.app.data.remote.api.QuoteApi
import com.btween.app.data.remote.api.UserApi
import com.btween.app.data.remote.dto.QuoteRequestDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.SourceType
import com.btween.app.domain.repository.SocialQuoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialQuoteRepositoryImpl @Inject constructor(
    private val quoteApi: QuoteApi,
    private val userApi: UserApi
) : SocialQuoteRepository {

    override suspend fun getFeed(
        limit: Int,
        offset: Long,
        scope: String,
        category: String?
    ): Result<List<SocialQuote>> = safeApiCall {
        quoteApi.getFeed(limit, offset, scope, category).map { it.toDomain() }
    }

    override suspend fun getQuote(id: Long): Result<SocialQuote> = safeApiCall {
        quoteApi.getQuote(id).toDomain()
    }

    override suspend fun getQuotesByTag(tag: String, limit: Int, offset: Long): Result<List<SocialQuote>> = safeApiCall {
        quoteApi.getQuotesByTag(tag, limit, offset).map { it.toDomain() }
    }

    override suspend fun getDailyQuote(): Result<SocialQuote> = safeApiCall {
        quoteApi.getDailyQuote().toDomain()
    }

    override suspend fun getUserQuotes(userId: Long, limit: Int, offset: Long): Result<List<SocialQuote>> =
        safeApiCall {
            userApi.getUserQuotes(userId, limit, offset).map { it.toDomain() }
        }

    override suspend fun createQuote(
        text: String,
        sourceTitle: String,
        sourceType: SourceType,
        speaker: String,
        author: String?,
        category: String?,
        tags: List<String>,
        imageUrl: String?,
        visibility: String
    ): Result<SocialQuote> = safeApiCall {
        quoteApi.createQuote(
            QuoteRequestDto(
                text = text,
                sourceTitle = sourceTitle,
                sourceType = sourceType.name,
                speaker = speaker,
                author = author,
                category = category,
                tags = tags,
                imageUrl = imageUrl,
                visibility = visibility
            )
        ).toDomain()
    }

    override suspend fun updateQuote(
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
    ): Result<SocialQuote> = safeApiCall {
        quoteApi.updateQuote(
            id,
            QuoteRequestDto(
                text = text,
                sourceTitle = sourceTitle,
                sourceType = sourceType.name,
                speaker = speaker,
                author = author,
                category = category,
                tags = tags,
                imageUrl = imageUrl,
                visibility = visibility
            )
        ).toDomain()
    }

    override suspend fun deleteQuote(id: Long): Result<Unit> = safeApiCall {
        quoteApi.deleteQuote(id)
    }

    override suspend fun likeQuote(id: Long): Result<Unit> = safeApiCall {
        quoteApi.likeQuote(id)
    }

    override suspend fun unlikeQuote(id: Long): Result<Unit> = safeApiCall {
        quoteApi.unlikeQuote(id)
    }
}
