package com.btweeu.app.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.SocialQuote
import com.btweeu.app.domain.repository.SocialQuoteRepository
import com.btweeu.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

data class TagQuotesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val quotes: List<SocialQuote> = emptyList(),
    val endReached: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TagQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val socialQuoteRepository: SocialQuoteRepository
) : ViewModel() {

    val tag: String = checkNotNull(savedStateHandle[Destination.TagQuotes.ARG_TAG])

    private val _uiState = MutableStateFlow(TagQuotesUiState())
    val uiState: StateFlow<TagQuotesUiState> = _uiState

    init {
        load()
    }

    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            socialQuoteRepository.getQuotesByTag(tag, limit = PAGE_SIZE, offset = 0)
                .onSuccess { quotes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        quotes = quotes,
                        endReached = quotes.size < PAGE_SIZE,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, errorMessage = error.message)
                }
        }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.endReached || state.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            socialQuoteRepository.getQuotesByTag(tag, limit = PAGE_SIZE, offset = state.quotes.size.toLong())
                .onSuccess { newQuotes ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        quotes = _uiState.value.quotes + newQuotes,
                        endReached = newQuotes.size < PAGE_SIZE
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoadingMore = false, errorMessage = error.message)
                }
        }
    }

    fun onRefresh() = load(isRefresh = true)

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onToggleLike(quote: SocialQuote) {
        val optimistic = quote.copy(
            isLikedByMe = !quote.isLikedByMe,
            likeCount = if (quote.isLikedByMe) quote.likeCount - 1 else quote.likeCount + 1
        )
        _uiState.value = _uiState.value.copy(
            quotes = _uiState.value.quotes.map { if (it.id == quote.id) optimistic else it }
        )

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    quotes = _uiState.value.quotes.map { if (it.id == quote.id) quote else it },
                    errorMessage = error.message
                )
            }
        }
    }
}
