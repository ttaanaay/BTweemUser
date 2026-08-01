package com.btween.app.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.QuoteCollection
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.User
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.CollectionRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val quotes: List<SocialQuote> = emptyList(),
    val collections: List<QuoteCollection> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isFollowActionInFlight: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val socialQuoteRepository: SocialQuoteRepository,
    private val collectionRepository: CollectionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle[Destination.Profile.ARG_USER_ID])

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        load()
    }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            val isOwnProfile = authRepository.getCurrentUserId() == userId
            val userResult = profileRepository.getUser(userId)
            val quotesResult = profileRepository.getUserQuotes(userId)
            // Collections are personal, so only fetch them for the signed-in person's own
            // profile - there's no API to view someone else's collections, nor should there be.
            val collections = if (isOwnProfile) {
                collectionRepository.getCollections().getOrDefault(emptyList())
            } else {
                emptyList()
            }

            userResult
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        user = user,
                        quotes = quotesResult.getOrDefault(emptyList()),
                        collections = collections,
                        isOwnProfile = isOwnProfile,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun onRefresh() = load(isRefresh = true)

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onToggleFollow() {
        val user = _uiState.value.user ?: return
        if (_uiState.value.isFollowActionInFlight) return

        val optimistic = user.copy(
            isFollowedByMe = !user.isFollowedByMe,
            followerCount = if (user.isFollowedByMe) user.followerCount - 1 else user.followerCount + 1
        )
        _uiState.value = _uiState.value.copy(user = optimistic, isFollowActionInFlight = true)

        viewModelScope.launch {
            val result = if (optimistic.isFollowedByMe) {
                profileRepository.follow(user.id)
            } else {
                profileRepository.unfollow(user.id)
            }
            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isFollowActionInFlight = false)
                }
                .onFailure { error ->
                    // Roll back the optimistic update if the request failed.
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isFollowActionInFlight = false,
                        errorMessage = error.message
                    )
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
                applyQuoteRollback(quote, error.message)
            }
        }
    }

    fun onDeleteQuote(quoteId: Long) {
        val previousQuotes = _uiState.value.quotes
        _uiState.value = _uiState.value.copy(quotes = previousQuotes.filterNot { it.id == quoteId })

        viewModelScope.launch {
            socialQuoteRepository.deleteQuote(quoteId).onFailure { error ->
                // Roll back if the delete failed server-side.
                _uiState.value = _uiState.value.copy(quotes = previousQuotes, errorMessage = error.message)
            }
        }
    }

    private fun applyQuoteRollback(original: SocialQuote, errorMessage: String?) {
        _uiState.value = _uiState.value.copy(
            quotes = _uiState.value.quotes.map { if (it.id == original.id) original else it },
            errorMessage = errorMessage
        )
    }
}
