package com.btweeu.app.ui.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.QuoteCollection
import com.btweeu.app.domain.model.SocialQuote
import com.btweeu.app.domain.repository.CollectionRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import com.btweeu.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val collection: QuoteCollection? = null,
    val quotes: List<SocialQuote> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val collectionRepository: CollectionRepository,
    private val socialQuoteRepository: SocialQuoteRepository
) : ViewModel() {

    private val collectionId: Long = checkNotNull(savedStateHandle[Destination.CollectionDetail.ARG_COLLECTION_ID])

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState

    init {
        load()
    }

    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            collectionRepository.getCollectionQuotes(collectionId)
                .onSuccess { (collection, quotes) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        collection = collection,
                        quotes = quotes,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, errorMessage = error.message)
                }
        }
    }

    fun onRefresh() = load(isRefresh = true)

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onRemoveFromCollection(quoteId: Long) {
        val previous = _uiState.value.quotes
        _uiState.value = _uiState.value.copy(quotes = previous.filterNot { it.id == quoteId })

        viewModelScope.launch {
            collectionRepository.removeItem(collectionId, quoteId).onFailure { error ->
                _uiState.value = _uiState.value.copy(quotes = previous, errorMessage = error.message)
            }
        }
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
