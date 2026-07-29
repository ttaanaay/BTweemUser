package com.btween.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.repository.NotificationRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import com.btween.app.domain.repository.TopContributor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dailyQuote: SocialQuote? = null,
    val trending: List<SocialQuote> = emptyList(),
    val recentlyApproved: List<SocialQuote> = emptyList(),
    val topContributors: List<TopContributor> = emptyList(),
    val unreadNotificationCount: Long = 0,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && dailyQuote == null && trending.isEmpty() && recentlyApproved.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val profileRepository: ProfileRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        load()
    }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)

            val feedResult = socialQuoteRepository.getFeed(limit = 30)
            val contributors = profileRepository.getTopContributors(limit = 10).getOrDefault(emptyList())
            val unreadCount = notificationRepository.getUnreadCount().getOrDefault(0)

            feedResult
                .onSuccess { quotes ->
                    val byLikes = quotes.sortedByDescending { it.likeCount }
                    val daily = byLikes.firstOrNull()
                    val trending = byLikes.filter { it.id != daily?.id }.take(6)
                    val recentlyApproved = quotes.filter { it.id != daily?.id }.take(6)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        dailyQuote = daily,
                        trending = trending,
                        recentlyApproved = recentlyApproved,
                        topContributors = contributors,
                        unreadNotificationCount = unreadCount,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        topContributors = contributors,
                        unreadNotificationCount = unreadCount,
                        errorMessage = error.message
                    )
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
        applyQuoteUpdate(optimistic)

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure {
                applyQuoteUpdate(quote) // roll back
                _uiState.value = _uiState.value.copy(errorMessage = it.message)
            }
        }
    }

    /** Applies an updated quote to whichever of the three sections currently contain it. */
    private fun applyQuoteUpdate(updated: SocialQuote) {
        val state = _uiState.value
        _uiState.value = state.copy(
            dailyQuote = if (state.dailyQuote?.id == updated.id) updated else state.dailyQuote,
            trending = state.trending.map { if (it.id == updated.id) updated else it },
            recentlyApproved = state.recentlyApproved.map { if (it.id == updated.id) updated else it }
        )
    }
}
