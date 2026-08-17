package com.btweeu.app.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.data.remote.CloudinaryUploader
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val displayName: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val errorMessage: String? = null,
    val didSave: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    init {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            viewModelScope.launch {
                profileRepository.getUser(currentUserId)
                    .onSuccess { user ->
                        _uiState.value = _uiState.value.copy(
                            displayName = user.displayName,
                            avatarUrl = user.avatarUrl.orEmpty(),
                            bio = user.bio.orEmpty(),
                            isLoading = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                    }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onDisplayNameChanged(value: String) = update { it.copy(displayName = value) }
    fun onAvatarUrlChanged(value: String) = update { it.copy(avatarUrl = value) }
    fun onBioChanged(value: String) = update { it.copy(bio = value) }
    fun consumeError() = update { it.copy(errorMessage = null) }

    private inline fun update(block: (EditProfileUiState) -> EditProfileUiState) {
        _uiState.value = block(_uiState.value)
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            update { it.copy(isUploadingPhoto = true) }
            cloudinaryUploader.uploadImage(uri)
                .onSuccess { url -> update { it.copy(isUploadingPhoto = false, avatarUrl = url) } }
                .onFailure { error -> update { it.copy(isUploadingPhoto = false, errorMessage = error.message) } }
        }
    }

    fun onSave() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            profileRepository.updateProfile(
                displayName = state.displayName.trim().takeIf { it.isNotEmpty() },
                avatarUrl = state.avatarUrl.trim().takeIf { it.isNotEmpty() },
                bio = state.bio.trim().takeIf { it.isNotEmpty() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, didSave = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                }
        }
    }
}
