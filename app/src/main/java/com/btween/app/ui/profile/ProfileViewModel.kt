package com.btween.app.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.model.User
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val quotes: List<SocialQuote> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isFollowActionInFlight: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle[Destination.Profile.ARG_USER_ID])

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userResult = profileRepository.getUser(userId)
            val quotesResult = profileRepository.getUserQuotes(userId)

            userResult
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        quotes = quotesResult.getOrDefault(emptyList()),
                        isOwnProfile = authRepository.getCurrentUserId() == userId,
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
}
