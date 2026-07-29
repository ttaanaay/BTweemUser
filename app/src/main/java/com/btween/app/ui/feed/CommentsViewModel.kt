package com.btween.app.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.Comment
import com.btween.app.domain.repository.AuthRepository
import com.btween.app.domain.repository.CommentRepository
import com.btween.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentsUiState(
    val isLoading: Boolean = true,
    val comments: List<Comment> = emptyList(),
    val draftText: String = "",
    val isPosting: Boolean = false,
    val currentUserId: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val quoteId: Long = checkNotNull(savedStateHandle[Destination.Comments.ARG_QUOTE_ID])

    private val _uiState = MutableStateFlow(CommentsUiState(currentUserId = authRepository.getCurrentUserId()))
    val uiState: StateFlow<CommentsUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            commentRepository.getComments(quoteId)
                .onSuccess { comments ->
                    _uiState.value = _uiState.value.copy(isLoading = false, comments = comments, errorMessage = null)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
                }
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(draftText = value)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onPostComment() {
        val text = _uiState.value.draftText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true)
            commentRepository.addComment(quoteId, text)
                .onSuccess { comment ->
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        draftText = "",
                        comments = _uiState.value.comments + comment
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isPosting = false, errorMessage = error.message)
                }
        }
    }

    fun onDeleteComment(id: Long) {
        val previousComments = _uiState.value.comments
        _uiState.value = _uiState.value.copy(comments = previousComments.filterNot { it.id == id })

        viewModelScope.launch {
            commentRepository.deleteComment(id).onFailure { error ->
                _uiState.value = _uiState.value.copy(comments = previousComments, errorMessage = error.message)
            }
        }
    }
}
