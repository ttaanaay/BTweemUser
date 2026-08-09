package com.btween.app.domain.repository

import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.QuoteFilter
import kotlinx.coroutines.flow.Flow

data class QuoteAutocompleteSuggestions(
    val sourceTitles: List<String> = emptyList(),
    val speakers: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

interface QuoteRepository {

    fun observeFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>>

    fun observeQuoteById(id: Long): Flow<Quote?>

    suspend fun getQuoteById(id: Long): Quote?

    fun observeFavorites(): Flow<List<Quote>>

    fun observeRecentlyAdded(limit: Int = 10): Flow<List<Quote>>

    fun observeRecentlyViewed(limit: Int = 10): Flow<List<Quote>>

    fun observeTotalCount(): Flow<Int>

    fun observeFavoriteCount(): Flow<Int>

    fun observeDistinctSourceCount(): Flow<Int>

    suspend fun addQuote(quote: Quote): Long

    suspend fun updateQuote(quote: Quote)

    suspend fun deleteQuote(quote: Quote)

    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    suspend fun markViewed(id: Long)

    suspend fun getAllQuotesOnce(): List<Quote>

    /** Every previously-used value for each Add/Edit form field, for autocomplete
     * suggestions - most-recently-added quotes first isn't tracked here, just distinct
     * values, since the DB query behind this can't cheaply preserve recency ordering for
     * the tags list (they're stored as a single converted column, not a normalized table). */
    suspend fun getAutocompleteSuggestions(): QuoteAutocompleteSuggestions

    suspend fun clearAllQuotes()

    suspend fun restoreQuotes(quotes: List<Quote>)
}
