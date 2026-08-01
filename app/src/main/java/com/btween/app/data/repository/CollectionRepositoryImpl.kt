package com.btween.app.data.repository

import com.btween.app.data.remote.api.CollectionApi
import com.btween.app.data.remote.dto.AddItemRequestDto
import com.btween.app.data.remote.dto.CollectionRequestDto
import com.btween.app.data.remote.dto.toDomain
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.model.QuoteCollection
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.repository.CollectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionApi: CollectionApi
) : CollectionRepository {

    override suspend fun getCollections(): Result<List<QuoteCollection>> = safeApiCall {
        collectionApi.getCollections().map {
            QuoteCollection(id = it.id, name = it.name, quoteCount = it.quoteCount, coverImageUrl = it.coverImageUrl, createdAt = it.createdAt)
        }
    }

    override suspend fun createCollection(name: String): Result<QuoteCollection> = safeApiCall {
        val response = collectionApi.createCollection(CollectionRequestDto(name))
        QuoteCollection(id = response.id, name = response.name, quoteCount = response.quoteCount, createdAt = response.createdAt)
    }

    override suspend fun getCollectionQuotes(id: Long): Result<Pair<QuoteCollection, List<SocialQuote>>> = safeApiCall {
        val response = collectionApi.getCollection(id)
        val collection = QuoteCollection(
            id = response.id,
            name = response.name,
            quoteCount = response.quotes.size,
            createdAt = response.createdAt
        )
        collection to response.quotes.map { it.toDomain() }
    }

    override suspend fun deleteCollection(id: Long): Result<Unit> = safeApiCall {
        collectionApi.deleteCollection(id)
    }

    override suspend fun addItem(collectionId: Long, quoteId: Long): Result<Unit> = safeApiCall {
        collectionApi.addItem(collectionId, AddItemRequestDto(quoteId))
    }

    override suspend fun removeItem(collectionId: Long, quoteId: Long): Result<Unit> = safeApiCall {
        collectionApi.removeItem(collectionId, quoteId)
    }
}
