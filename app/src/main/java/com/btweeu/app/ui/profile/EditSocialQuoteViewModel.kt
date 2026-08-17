package com.btweeu.app.ui.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.data.remote.CloudinaryUploader
import com.btweeu.app.domain.model.SourceType
import com.btweeu.app.domain.repository.PublicCategory
import com.btweeu.app.domain.repository.PublicCategoryRepository
import com.btweeu.app.domain.repository.PublicSourceTypeRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import com.btweeu.app.domain.usecase.quote.GetAutocompleteSuggestionsUseCase
import com.btweeu.app.domain.usecase.quote.QuoteAutocompleteSuggestions
import com.btweeu.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditSocialQuoteUiState(
    val text: String = "",
    val sourceTitle: String = "",
    val sourceType: String = SourceType.MOVIE,
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
    private val getAutocompleteSuggestionsUseCase: GetAutocompleteSuggestionsUseCase,
    private val publicSourceTypeRepository: PublicSourceTypeRepository,
    private val publicCategoryRepository: PublicCategoryRepository
) : ViewModel() {

    private val quoteId: Long = checkNotNull(savedStateHandle[Destination.EditSocialQuote.ARG_QUOTE_ID])

    private val _uiState = MutableStateFlow(EditSocialQuoteUiState())
    val uiState: StateFlow<EditSocialQuoteUiState> = _uiState

    private val _sourceTypeOptions = MutableStateFlow(SourceType.DEFAULT_OPTIONS)
    val sourceTypeOptions: StateFlow<List<String>> = _sourceTypeOptions

    private val _categoryOptions = MutableStateFlow<List<PublicCategory>>(emptyList())
    val categoryOptions: StateFlow<List<PublicCategory>> = _categoryOptions

    private val _suggestions = MutableStateFlow(QuoteAutocompleteSuggestions())
    val suggestions: StateFlow<QuoteAutocompleteSuggestions> = _suggestions

    init {
        viewModelScope.launch {
            publicSourceTypeRepository.getSourceTypes().onSuccess { types ->
                if (types.isNotEmpty()) _sourceTypeOptions.value = types
            }
        }
        viewModelScope.launch {
            publicCategoryRepository.getCategories().onSuccess { categories ->
                _categoryOptions.value = categories
            }
        }
        viewModelScope.launch {
            _suggestions.value = getAutocompleteSuggestionsUseCase()
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
    fun onSourceTypeChanged(value: String) = update { it.copy(sourceType = value) }
    fun onCategoryChanged(value: String?) = update { it.copy(category = value) }
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
