package com.btween.app.ui.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.data.remote.CloudinaryUploader
import com.btween.app.domain.model.SourceType
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.QuoteAutocompleteSuggestions
import com.btween.app.domain.repository.QuoteRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditSocialQuoteUiState(
    val text: String = "",
    val sourceTitle: String = "",
    val sourceType: SourceType = SourceType.MOVIE,
    val speaker: String = "",
    val author: String = "",
    val visibility: String = "PUBLIC",
    val category: String? = null,
    val tagsInput: String = "",
    val imageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val didSave: Boolean = false,
    val errorMessage: String? = null
) {
    val tags: List<String> get() = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@HiltViewModel
class EditSocialQuoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val socialQuoteRepository: SocialQuoteRepository,
    private val cloudinaryUploader: CloudinaryUploader,
    private val quoteRepository: QuoteRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val quoteId: Long = checkNotNull(savedStateHandle[Destination.EditSocialQuote.ARG_QUOTE_ID])

    private val _uiState = MutableStateFlow(EditSocialQuoteUiState())
    val uiState: StateFlow<EditSocialQuoteUiState> = _uiState

    private val _suggestions = MutableStateFlow(QuoteAutocompleteSuggestions())
    val suggestions: StateFlow<QuoteAutocompleteSuggestions> = _suggestions

    init {
        viewModelScope.launch {
            val local = quoteRepository.getAutocompleteSuggestions()
            val userId = authRepository.getCurrentUserId()
            val social = userId?.let {
                profileRepository.getUserQuotes(it, limit = 200).getOrDefault(emptyList())
            }.orEmpty()

            _suggestions.value = QuoteAutocompleteSuggestions(
                sourceTitles = (local.sourceTitles + social.map { it.sourceTitle })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                speakers = (local.speakers + social.map { it.speaker })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                authors = (local.authors + social.mapNotNull { it.author })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                tags = (local.tags + social.flatMap { it.tags })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
            )
        }
        viewModelScope.launch {
            socialQuoteRepository.getQuote(quoteId)
                .onSuccess { quote ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        text = quote.text,
                        sourceTitle = quote.sourceTitle,
                        sourceType = quote.sourceType,
                        speaker = quote.speaker,
                        author = quote.author.orEmpty(),
                        visibility = quote.visibility,
                        category = quote.category,
                        tagsInput = quote.tags.joinToString(", "),
                        imageUrl = quote.imageUrl
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }

    fun onTextChanged(value: String) = update { it.copy(text = value) }
    fun onSourceTitleChanged(value: String) = update { it.copy(sourceTitle = value) }
    fun onSpeakerChanged(value: String) = update { it.copy(speaker = value) }
    fun onAuthorChanged(value: String) = update { it.copy(author = value) }
    fun onTagsInputChanged(value: String) = update { it.copy(tagsInput = value) }
    fun onSourceTypeChanged(value: SourceType) = update { it.copy(sourceType = value) }
    fun onRemoveImage() = update { it.copy(imageUrl = null) }
    fun consumeError() = update { it.copy(errorMessage = null) }

    private inline fun update(block: (EditSocialQuoteUiState) -> EditSocialQuoteUiState) {
        _uiState.value = block(_uiState.value)
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            update { it.copy(isUploadingImage = true) }
            cloudinaryUploader.uploadImage(uri)
                .onSuccess { url -> update { it.copy(isUploadingImage = false, imageUrl = url) } }
                .onFailure { error -> update { it.copy(isUploadingImage = false, errorMessage = error.message) } }
        }
    }

    fun onSave() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            socialQuoteRepository.updateQuote(
                id = quoteId,
                text = state.text.trim(),
                sourceTitle = state.sourceTitle.trim(),
                sourceType = state.sourceType,
                speaker = state.speaker.trim(),
                author = state.author.trim().takeIf { it.isNotEmpty() },
                category = state.category,
                tags = state.tags,
                imageUrl = state.imageUrl,
                visibility = state.visibility
            )
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, didSave = true) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                }
        }
    }
}
