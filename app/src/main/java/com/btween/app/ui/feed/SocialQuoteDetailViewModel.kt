package com.btween.app.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialQuoteDetailUiState(
    val isLoading: Boolean = true,
    val quote: SocialQuote? = null,
    val isOwnQuote: Boolean = false,
    val isFollowActionInFlight: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SocialQuoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val socialQuoteRepository: SocialQuoteRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val quoteId: Long = checkNotNull(savedStateHandle[Destination.SocialQuoteDetail.ARG_QUOTE_ID])

    private val _uiState = MutableStateFlow(SocialQuoteDetailUiState())
    val uiState: StateFlow<SocialQuoteDetailUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            socialQuoteRepository.getQuote(quoteId)
                .onSuccess { quote ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quote = quote,
                        isOwnQuote = authRepository.getCurrentUserId() == quote.owner.id,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onToggleLike() {
        val quote = _uiState.value.quote ?: return
        val optimistic = quote.copy(
            isLikedByMe = !quote.isLikedByMe,
            likeCount = if (quote.isLikedByMe) quote.likeCount - 1 else quote.likeCount + 1
        )
        _uiState.value = _uiState.value.copy(quote = optimistic)

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(quote = quote, errorMessage = error.message)
            }
        }
    }

    fun onToggleFollow() {
        val quote = _uiState.value.quote ?: return
        if (_uiState.value.isFollowActionInFlight) return
        val owner = quote.owner

        val optimisticOwner = owner.copy(
            isFollowedByMe = !owner.isFollowedByMe,
            followerCount = if (owner.isFollowedByMe) owner.followerCount - 1 else owner.followerCount + 1
        )
        _uiState.value = _uiState.value.copy(
            quote = quote.copy(owner = optimisticOwner),
            isFollowActionInFlight = true
        )

        viewModelScope.launch {
            val result = if (optimisticOwner.isFollowedByMe) {
                profileRepository.follow(owner.id)
            } else {
                profileRepository.unfollow(owner.id)
            }
            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isFollowActionInFlight = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        quote = quote,
                        isFollowActionInFlight = false,
                        errorMessage = error.message
                    )
                }
        }
    }
}
