package com.btween.app.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.SourceType
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
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val didSave: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditSocialQuoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val socialQuoteRepository: SocialQuoteRepository
) : ViewModel() {

    private val quoteId: Long = checkNotNull(savedStateHandle[Destination.EditSocialQuote.ARG_QUOTE_ID])

    private val _uiState = MutableStateFlow(EditSocialQuoteUiState())
    val uiState: StateFlow<EditSocialQuoteUiState> = _uiState

    init {
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
                        tags = quote.tags
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
    fun onSourceTypeChanged(value: SourceType) = update { it.copy(sourceType = value) }
    fun consumeError() = update { it.copy(errorMessage = null) }

    private inline fun update(block: (EditSocialQuoteUiState) -> EditSocialQuoteUiState) {
        _uiState.value = block(_uiState.value)
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
                visibility = state.visibility
            )
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, didSave = true) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                }
        }
    }
}
