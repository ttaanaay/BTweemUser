package com.btweeu.app.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.User
import com.btweeu.app.domain.repository.ProfileRepository
import com.btweeu.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FollowListType { FOLLOWERS, FOLLOWING }

data class FollowListUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class FollowListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle[Destination.FollowList.ARG_USER_ID])
    val listType: FollowListType = FollowListType.valueOf(
        checkNotNull(savedStateHandle[Destination.FollowList.ARG_TYPE])
    )

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = if (listType == FollowListType.FOLLOWERS) {
                profileRepository.getFollowers(userId)
            } else {
                profileRepository.getFollowing(userId)
            }
            result
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(isLoading = false, users = users, errorMessage = null)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }
}
