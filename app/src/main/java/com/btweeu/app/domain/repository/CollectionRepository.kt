package com.btweeu.app.domain.repository

import com.btweeu.app.domain.model.QuoteCollection
import com.btweeu.app.domain.model.SocialQuote

interface CollectionRepository {

    suspend fun getCollections(): Result<List<QuoteCollection>>

    suspend fun createCollection(name: String): Result<QuoteCollection>

    suspend fun getCollectionQuotes(id: Long): Result<Pair<QuoteCollection, List<SocialQuote>>>

    suspend fun deleteCollection(id: Long): Result<Unit>

    suspend fun addItem(collectionId: Long, quoteId: Long): Result<Unit>

    suspend fun removeItem(collectionId: Long, quoteId: Long): Result<Unit>
}
