package com.btween.app.ui.addedit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.data.remote.CloudinaryUploader
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.SourceType
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.repository.PublicCategory
import com.btween.app.domain.repository.PublicCategoryRepository
import com.btween.app.domain.repository.PublicSourceTypeRepository
import com.btween.app.domain.repository.QuoteAutocompleteSuggestions
import com.btween.app.domain.repository.QuoteRepository
import com.btween.app.domain.repository.SocialQuoteRepository
import com.btween.app.domain.usecase.quote.AddQuoteUseCase
import com.btween.app.domain.usecase.quote.UpdateQuoteUseCase
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditFormState(
    val quoteId: Long? = null,
    val text: String = "",
    val sourceTitle: String = "",
    val sourceType: String = SourceType.MOVIE,
    val speaker: String = "",
    val author: String = "",
    // A single admin-managed category, shared between local storage and the feed post if
    // shared - there's no separate local-only category system anymore.
    val category: String? = null,
    val tagsInput: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val shareToFeed: Boolean = false,
    val imageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val didSave: Boolean = false,
    val needsLogin: Boolean = false
) {
    val isEditMode: Boolean get() = quoteId != null
    val tags: List<String> get() = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quoteRepository: QuoteRepository,
    private val socialQuoteRepository: SocialQuoteRepository,
    private val profileRepository: ProfileRepository,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val updateQuoteUseCase: UpdateQuoteUseCase,
    private val cloudinaryUploader: CloudinaryUploader,
    private val authRepository: AuthRepository,
    private val publicSourceTypeRepository: PublicSourceTypeRepository,
    private val publicCategoryRepository: PublicCategoryRepository
) : ViewModel() {

    private val requestedId: Long = savedStateHandle.get<Long>(Destination.AddEditQuote.ARG_QUOTE_ID)
        ?.takeIf { it != Destination.AddEditQuote.NEW_QUOTE_ID } ?: -1L

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
            val local = quoteRepository.getAutocompleteSuggestions()

            // Most of a person's actual quote history often lives on the server (quotes
            // shared publicly) rather than the local-only library, so local-only
            // suggestions were nearly always empty in practice. Merge in their own social
            // quotes too, so autocomplete reflects everything they've ever entered.
            val userId = authRepository.getCurrentUserId()
            val social = userId?.let {
                profileRepository.getUserQuotes(it, limit = 200).getOrDefault(emptyList())
            }.orEmpty()

            // Also merge in a slice of the public feed, so autocomplete is useful even on
            // someone's very first quote, before they have any history of their own to draw
            // from - matches the same fix already made on the web app.
            val community = socialQuoteRepository.getFeed(limit = 200).getOrDefault(emptyList())

            _suggestions.value = QuoteAutocompleteSuggestions(
                sourceTitles = (local.sourceTitles + social.map { it.sourceTitle } + community.map { it.sourceTitle })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                speakers = (local.speakers + social.map { it.speaker } + community.map { it.speaker })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                authors = (local.authors + social.mapNotNull { it.author } + community.mapNotNull { it.author })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER),
                tags = (local.tags + social.flatMap { it.tags } + community.flatMap { it.tags })
                    .filter { it.isNotBlank() }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
            )
        }
        if (requestedId > 0) {
            viewModelScope.launch {
                val existing = quoteRepository.getQuoteById(requestedId)
                _formState.value = if (existing != null) {
                    AddEditFormState(
                        quoteId = existing.id,
                        text = existing.text,
                        sourceTitle = existing.sourceTitle,
                        sourceType = existing.sourceType,
                        speaker = existing.speaker,
                        author = existing.author.orEmpty(),
                        category = existing.category,
                        tagsInput = existing.tags.joinToString(", "),
                        note = existing.note.orEmpty(),
                        isFavorite = existing.isFavorite,
                        isLoading = false
                    )
                } else {
                    _formState.value.copy(isLoading = false, errorMessage = "Quote not found")
                }
            }
        } else {
            _formState.value = _formState.value.copy(isLoading = false)
        }
    }

    fun onTextChanged(value: String) = update { it.copy(text = value) }
    fun onSourceTitleChanged(value: String) = update { it.copy(sourceTitle = value) }
    fun onSourceTypeChanged(value: String) = update { it.copy(sourceType = value) }
    fun onSpeakerChanged(value: String) = update { it.copy(speaker = value) }
    fun onAuthorChanged(value: String) = update { it.copy(author = value) }
    fun onCategoryChanged(value: String?) = update { it.copy(category = value) }
    fun onTagsInputChanged(value: String) = update { it.copy(tagsInput = value) }
    fun onNoteChanged(value: String) = update { it.copy(note = value) }
    fun onFavoriteToggled() = update { it.copy(isFavorite = !it.isFavorite) }
    fun onShareToFeedToggled() {
        if (!_formState.value.shareToFeed && authRepository.getCurrentUserId() == null) {
            update { it.copy(needsLogin = true) }
            return
        }
        update { it.copy(shareToFeed = !it.shareToFeed) }
    }
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
        val state = _formState.value
        val quote = Quote(
            id = state.quoteId ?: 0L,
            text = state.text.trim(),
            sourceTitle = state.sourceTitle.trim(),
            sourceType = state.sourceType,
            speaker = state.speaker.trim(),
            author = state.author.trim().takeIf { it.isNotEmpty() },
            category = state.category,
            tags = state.tags,
            note = state.note.trim().takeIf { it.isNotEmpty() },
            isFavorite = state.isFavorite
        )

        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSaving = true)
            val result = if (state.isEditMode) updateQuoteUseCase(quote) else addQuoteUseCase(quote).map { }

            result
                .onSuccess {
                    // The quote is safely stored locally no matter what happens next - sharing
                    // publicly is a secondary, best-effort step that never undoes the local save.
                    var shareWarning: String? = null
                    if (!state.isEditMode && state.shareToFeed) {
                        socialQuoteRepository.createQuote(
                            text = quote.text,
                            sourceTitle = quote.sourceTitle,
                            sourceType = quote.sourceType,
                            speaker = quote.speaker,
                            author = quote.author,
                            category = state.category,
                            tags = quote.tags,
                            imageUrl = state.imageUrl,
                            visibility = "PUBLIC"
                        ).onFailure { error ->
                            shareWarning = "Saved, but couldn't share to Feed: ${error.message}"
                        }
                    }
                    _formState.value = _formState.value.copy(
                        isSaving = false,
                        didSave = true,
                        errorMessage = shareWarning
                    )
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Something went wrong"
                    )
                }
        }
    }
}
