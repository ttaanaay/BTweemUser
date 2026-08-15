package com.btween.app.data.repository

import com.btween.app.data.local.dao.QuoteDao
import com.btween.app.data.local.mapper.toDomain
import com.btween.app.data.local.mapper.toEntity
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.QuoteFilter
import com.btween.app.domain.repository.QuoteAutocompleteSuggestions
import com.btween.app.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteRepository {

    override fun observeFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>> =
        quoteDao.observeFilteredQuotes(
            category = filter.category,
            sourceType = filter.sourceType,
            favoritesOnly = filter.favoritesOnly,
            searchQuery = filter.searchQuery.trim(),
            sortOrder = filter.sortOrder.ordinal
        ).map { entities -> entities.map { it.toDomain() } }

    override fun observeQuoteById(id: Long): Flow<Quote?> =
        quoteDao.observeQuoteById(id).map { it?.toDomain() }

    override suspend fun getQuoteById(id: Long): Quote? =
        quoteDao.getQuoteById(id)?.toDomain()

    override fun observeFavorites(): Flow<List<Quote>> =
        quoteDao.observeFavorites().map { entities -> entities.map { it.toDomain() } }

    override fun observeRecentlyAdded(limit: Int): Flow<List<Quote>> =
        quoteDao.observeRecentlyAdded(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeRecentlyViewed(limit: Int): Flow<List<Quote>> =
        quoteDao.observeRecentlyViewed(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeTotalCount(): Flow<Int> = quoteDao.observeTotalCount()

    override fun observeFavoriteCount(): Flow<Int> = quoteDao.observeFavoriteCount()

    override fun observeDistinctSourceCount(): Flow<Int> = quoteDao.observeDistinctSourceCount()

    override suspend fun addQuote(quote: Quote): Long {
        val now = System.currentTimeMillis()
        return quoteDao.insert(quote.copy(createdAt = now, updatedAt = now).toEntity())
    }

    override suspend fun updateQuote(quote: Quote) {
        quoteDao.update(quote.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun deleteQuote(quote: Quote) {
        quoteDao.delete(quote.toEntity())
    }

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        quoteDao.setFavorite(id, isFavorite, System.currentTimeMillis())
    }

    override suspend fun markViewed(id: Long) {
        quoteDao.markViewed(id, System.currentTimeMillis())
    }

    override suspend fun getAllQuotesOnce(): List<Quote> =
        quoteDao.getAllQuotesOnce().map { it.toDomain() }

    override suspend fun getAutocompleteSuggestions(): QuoteAutocompleteSuggestions {
        // tags is a TypeConverter-backed List<String> column - Room can't map a raw query
        // straight to List<List<String>> (that's what broke the build), so pull it from the
        // full entities instead, which Room already knows how to convert correctly.
        val tags = quoteDao.getAllQuotesOnce()
            .flatMap { it.tags }
            .distinct()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return QuoteAutocompleteSuggestions(
            sourceTitles = quoteDao.getDistinctSourceTitles(),
            speakers = quoteDao.getDistinctSpeakers(),
            authors = quoteDao.getDistinctAuthors(),
            tags = tags
        )
    }

    override suspend fun clearAllQuotes() {
        quoteDao.deleteAll()
    }

    override suspend fun restoreQuotes(quotes: List<Quote>) {
        quoteDao.insertAll(quotes.map { it.toEntity() })
    }
}
