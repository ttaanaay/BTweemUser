package com.btweeu.app.ui.addedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.data.remote.CloudinaryUploader
import com.btweeu.app.domain.model.SourceType
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.PublicCategory
import com.btweeu.app.domain.repository.PublicCategoryRepository
import com.btweeu.app.domain.repository.PublicSourceTypeRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import com.btweeu.app.domain.usecase.quote.GetAutocompleteSuggestionsUseCase
import com.btweeu.app.domain.usecase.quote.QuoteAutocompleteSuggestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditFormState(
    val text: String = "",
    val sourceTitle: String = "",
    val sourceType: String = SourceType.MOVIE,
    val speaker: String = "",
    val author: String = "",
    val category: String? = null,
    val tagsInput: String = "",
    // Every quote now lives on the server - this just controls who can see it. PRIVATE
    // quotes still show up in your own Library, they just don't appear in the public feed.
    val visibility: String = "PUBLIC",
    val imageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val didSave: Boolean = false,
    val needsLogin: Boolean = false
) {
    val tags: List<String> get() = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

/**
 * Always creates a quote on the server - there's no local-only save anymore. Editing an
 * existing quote is handled by a separate screen (EditSocialQuoteScreen); this one is
 * create-only.
 */
@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val cloudinaryUploader: CloudinaryUploader,
    private val authRepository: AuthRepository,
    private val publicSourceTypeRepository: PublicSourceTypeRepository,
    private val publicCategoryRepository: PublicCategoryRepository,
    private val getAutocompleteSuggestionsUseCase: GetAutocompleteSuggestionsUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(AddEditFormState())
    val formState: StateFlow<AddEditFormState> = _formState

    private val _sourceTypeOptions = MutableStateFlow(SourceType.DEFAULT_OPTIONS)
    val sourceTypeOptions: StateFlow<List<String>> = _sourceTypeOptions

    private val _categoryOptions = MutableStateFlow<List<PublicCategory>>(emptyList())
    val categoryOptions: StateFlow<List<PublicCategory>> = _categoryOptions

    private val _suggestions = MutableStateFlow(QuoteAutocompleteSuggestions())
    val suggestions: StateFlow<QuoteAutocompleteSuggestions> = _suggestions

    init {
        viewModelScope.launch {
            // Falls back to the original fixed list (already the initial value) if this
            // fails - the form should still be usable offline or if the request errors.
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
    }

    fun onTextChanged(value: String) = update { it.copy(text = value) }
    fun onSourceTitleChanged(value: String) = update { it.copy(sourceTitle = value) }
    fun onSourceTypeChanged(value: String) = update { it.copy(sourceType = value) }
    fun onSpeakerChanged(value: String) = update { it.copy(speaker = value) }
    fun onAuthorChanged(value: String) = update { it.copy(author = value) }
    fun onCategoryChanged(value: String?) = update { it.copy(category = value) }
    fun onTagsInputChanged(value: String) = update { it.copy(tagsInput = value) }
    fun onVisibilityChanged(value: String) = update { it.copy(visibility = value) }
    fun onRemoveImage() = update { it.copy(imageUrl = null) }
    fun consumeError() = update { it.copy(errorMessage = null) }
    fun consumeNeedsLogin() = update { it.copy(needsLogin = false) }

    private inline fun update(block: (AddEditFormState) -> AddEditFormState) {
        _formState.value = block(_formState.value)
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
        if (authRepository.getCurrentUserId() == null) {
            update { it.copy(needsLogin = true) }
            return
        }

        val state = _formState.value
        viewModelScope.launch {
            update { it.copy(isSaving = true) }
            socialQuoteRepository.createQuote(
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
                .onSuccess {
                    update { it.copy(isSaving = false, didSave = true) }
                }
                .onFailure { error ->
                    update { it.copy(isSaving = false, errorMessage = error.message ?: "Something went wrong") }
                }
        }
    }
}
