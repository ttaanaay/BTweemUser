package com.btweeu.app.domain.usecase.quote

import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import javax.inject.Inject

data class QuoteAutocompleteSuggestions(
    val sourceTitles: List<String> = emptyList(),
    val speakers: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * Merges the signed-in account's own quotes with a slice of the public feed, so autocomplete
 * is useful even on someone's very first quote (not just once they have history of their
 * own) - matches the same behavior on the web app.
 */
class GetAutocompleteSuggestionsUseCase @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): QuoteAutocompleteSuggestions {
        val userId = authRepository.getCurrentUserId()
        val own = userId?.let {
            socialQuoteRepository.getUserQuotes(it, limit = 200).getOrDefault(emptyList())
        }.orEmpty()
        val community = socialQuoteRepository.getFeed(limit = 200).getOrDefault(emptyList())
        val combined = own + community

        return QuoteAutocompleteSuggestions(
            sourceTitles = combined.map { it.sourceTitle }
                .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
            speakers = combined.map { it.speaker }
                .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
            authors = combined.mapNotNull { it.author }
                .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
            tags = combined.flatMap { it.tags }
                .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
        )
    }
}
