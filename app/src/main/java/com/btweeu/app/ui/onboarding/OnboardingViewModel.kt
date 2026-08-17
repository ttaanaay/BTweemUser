package com.btweeu.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.User
import com.btweeu.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val onboardingInterests = listOf("Life", "Love", "Motivation", "Success", "Wisdom", "Humor", "Books", "Movie")

data class OnboardingUiState(
    val step: Int = 0,
    val selectedInterests: Set<String> = emptySet(),
    val isLoadingSuggestions: Boolean = true,
    val suggestedUsers: List<User> = emptyList(),
    val followingIds: Set<Long> = emptySet(),
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun onToggleInterest(interest: String) {
        val current = _uiState.value.selectedInterests
        val updated = if (interest in current) current - interest else current + interest
        _uiState.value = _uiState.value.copy(selectedInterests = updated)
    }

    fun onContinueFromInterests() {
        _uiState.value = _uiState.value.copy(step = 1)
        loadSuggestedUsers()
    }

    fun onBackToInterests() {
        _uiState.value = _uiState.value.copy(step = 0)
    }

    private fun loadSuggestedUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSuggestions = true)
            profileRepository.getTopContributors(limit = 10)
                .onSuccess { contributors ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingSuggestions = false,
                        suggestedUsers = contributors.map { it.user }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoadingSuggestions = false, errorMessage = error.message)
                }
        }
    }

    fun onToggleFollow(userId: Long) {
        val isCurrentlyFollowing = userId in _uiState.value.followingIds
        _uiState.value = _uiState.value.copy(
            followingIds = if (isCurrentlyFollowing) {
                _uiState.value.followingIds - userId
            } else {
                _uiState.value.followingIds + userId
            }
        )

        viewModelScope.launch {
            val result = if (isCurrentlyFollowing) {
                profileRepository.unfollow(userId)
            } else {
                profileRepository.follow(userId)
            }
            result.onFailure {
                // Roll back optimistic toggle on failure.
                _uiState.value = _uiState.value.copy(
                    followingIds = if (isCurrentlyFollowing) {
                        _uiState.value.followingIds + userId
                    } else {
                        _uiState.value.followingIds - userId
                    }
                )
            }
        }
    }
}
