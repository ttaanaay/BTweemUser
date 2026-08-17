package com.btweeu.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.SocialQuote
import com.btweeu.app.domain.model.SortOrder
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.PublicCategory
import com.btweeu.app.domain.repository.PublicCategoryRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryFilterState(
    val searchQuery: String = "",
    val category: String? = null,
    val sourceType: String? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

data class LibraryUiState(
    val filter: LibraryFilterState = LibraryFilterState(),
    val quotes: List<SocialQuote> = emptyList(),
    val categories: List<PublicCategory> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * Shows every quote this account owns - both PUBLIC and PRIVATE - fetched from the server
 * (the backend already includes private quotes for the owner viewing their own list). There's
 * no more local-only library: every quote here is one you've actually posted, just possibly
 * with visibility set to Private so only you can see it.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val authRepository: AuthRepository,
    private val publicCategoryRepository: PublicCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    private var allQuotes: List<SocialQuote> = emptyList()

    init {
        load()
        viewModelScope.launch {
            publicCategoryRepository.getCategories().onSuccess { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun load() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            socialQuoteRepository.getUserQuotes(userId, limit = 100)
                .onSuccess { quotes ->
                    allQuotes = quotes
                    applyFilter()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun applyFilter() {
        val filter = _uiState.value.filter
        var result = allQuotes

        if (filter.searchQuery.isNotBlank()) {
            val needle = filter.searchQuery.trim().lowercase()
            result = result.filter {
                it.text.lowercase().contains(needle) ||
                    it.sourceTitle.lowercase().contains(needle) ||
                    it.speaker.lowercase().contains(needle) ||
                    it.author?.lowercase()?.contains(needle) == true ||
                    it.tags.any { tag -> tag.lowercase().contains(needle) }
            }
        }
        filter.category?.let { category -> result = result.filter { it.category == category } }
        filter.sourceType?.let { sourceType -> result = result.filter { it.sourceType == sourceType } }

        result = when (filter.sortOrder) {
            SortOrder.NEWEST -> result.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> result.sortedBy { it.createdAt }
            SortOrder.ALPHABETICAL -> result.sortedBy { it.text.lowercase() }
            // No local "favorite" flag anymore - liked-first is the closest equivalent using
            // data the server actually has.
            SortOrder.FAVORITE -> result.sortedWith(compareByDescending<SocialQuote> { it.isLikedByMe }.thenByDescending { it.createdAt })
        }

        _uiState.update { it.copy(quotes = result, isLoading = false) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchQuery = query)) }
        applyFilter()
    }

    fun onSortOrderSelected(order: SortOrder) {
        _uiState.update { it.copy(filter = it.filter.copy(sortOrder = order)) }
        applyFilter()
    }

    fun onCategorySelected(category: String?) {
        _uiState.update {
            it.copy(filter = it.filter.copy(category = if (it.filter.category == category) null else category))
        }
        applyFilter()
    }

    fun onSourceTypeSelected(sourceType: String?) {
        _uiState.update {
            it.copy(filter = it.filter.copy(sourceType = if (it.filter.sourceType == sourceType) null else sourceType))
        }
        applyFilter()
    }

    fun onToggleLike(quote: SocialQuote) {
        val optimistic = quote.copy(
            isLikedByMe = !quote.isLikedByMe,
            likeCount = if (quote.isLikedByMe) quote.likeCount - 1 else quote.likeCount + 1
        )
        allQuotes = allQuotes.map { if (it.id == quote.id) optimistic else it }
        applyFilter()

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure {
                allQuotes = allQuotes.map { q -> if (q.id == quote.id) quote else q } // roll back
                applyFilter()
            }
        }
    }

    fun onClearFilters() {
        _uiState.update { it.copy(filter = LibraryFilterState(searchQuery = it.filter.searchQuery, sortOrder = it.filter.sortOrder)) }
        applyFilter()
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }
}
