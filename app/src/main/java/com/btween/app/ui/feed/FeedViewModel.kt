package com.btween.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

enum class FeedTab(val scopeParam: String) {
    FOR_YOU("recommended"),
    FOLLOWING("following")
}

/** Per-tab feed state, tracked independently so switching tabs doesn't lose scroll position/data. */
data class FeedTabState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val quotes: List<SocialQuote> = emptyList(),
    val endReached: Boolean = false,
    val hasLoadedOnce: Boolean = false
)

data class FeedUiState(
    val selectedTab: FeedTab = FeedTab.FOR_YOU,
    val forYou: FeedTabState = FeedTabState(),
    val following: FeedTabState = FeedTabState(),
    val errorMessage: String? = null,
    val needsLogin: Boolean = false
) {
    val current: FeedTabState get() = if (selectedTab == FeedTab.FOR_YOU) forYou else following
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        loadTab(FeedTab.FOR_YOU)
    }

    fun onTabSelected(tab: FeedTab) {
        if (tab == FeedTab.FOLLOWING && authRepository.getCurrentUserId() == null) {
            _uiState.value = _uiState.value.copy(needsLogin = true)
            return
        }
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        val tabState = if (tab == FeedTab.FOR_YOU) _uiState.value.forYou else _uiState.value.following
        if (!tabState.hasLoadedOnce) loadTab(tab)
    }

    private fun updateTab(tab: FeedTab, block: (FeedTabState) -> FeedTabState) {
        _uiState.value = if (tab == FeedTab.FOR_YOU) {
            _uiState.value.copy(forYou = block(_uiState.value.forYou))
        } else {
            _uiState.value.copy(following = block(_uiState.value.following))
        }
    }

    private fun loadTab(tab: FeedTab, isRefresh: Boolean = false) {
        viewModelScope.launch {
            updateTab(tab) { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh) }
            socialQuoteRepository.getFeed(limit = PAGE_SIZE, offset = 0, scope = tab.scopeParam)
                .onSuccess { quotes ->
                    updateTab(tab) {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            quotes = quotes,
                            endReached = quotes.size < PAGE_SIZE,
                            hasLoadedOnce = true
                        )
                    }
                }
                .onFailure { error ->
                    updateTab(tab) { it.copy(isLoading = false, isRefreshing = false, hasLoadedOnce = true) }
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun onLoadMore() {
        val tab = _uiState.value.selectedTab
        val state = _uiState.value.current
        if (state.isLoadingMore || state.endReached || state.isLoading) return

        viewModelScope.launch {
            updateTab(tab) { it.copy(isLoadingMore = true) }
            socialQuoteRepository.getFeed(limit = PAGE_SIZE, offset = state.quotes.size.toLong(), scope = tab.scopeParam)
                .onSuccess { newQuotes ->
                    updateTab(tab) {
                        it.copy(
                            isLoadingMore = false,
                            quotes = it.quotes + newQuotes,
                            endReached = newQuotes.size < PAGE_SIZE
                        )
                    }
                }
                .onFailure { error ->
                    updateTab(tab) { it.copy(isLoadingMore = false) }
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun onRefresh() = loadTab(_uiState.value.selectedTab, isRefresh = true)

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeNeedsLogin() {
        _uiState.value = _uiState.value.copy(needsLogin = false)
    }

    fun onToggleLike(quote: SocialQuote) {
        if (authRepository.getCurrentUserId() == null) {
            _uiState.value = _uiState.value.copy(needsLogin = true)
            return
        }

        val tab = _uiState.value.selectedTab
        val optimistic = quote.copy(
            isLikedByMe = !quote.isLikedByMe,
            likeCount = if (quote.isLikedByMe) quote.likeCount - 1 else quote.likeCount + 1
        )
        updateTab(tab) { state ->
            state.copy(quotes = state.quotes.map { if (it.id == quote.id) optimistic else it })
        }

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure { error ->
                updateTab(tab) { state ->
                    state.copy(quotes = state.quotes.map { if (it.id == quote.id) quote else it })
                }
                _uiState.value = _uiState.value.copy(errorMessage = error.message)
            }
        }
    }
}
