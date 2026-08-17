package com.btweeu.app.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.Comment
import com.btweeu.app.domain.repository.AuthRepository
import com.btweeu.app.domain.repository.CommentRepository
import com.btweeu.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val draftText: String = "",
    val isPosting: Boolean = false,
    val editingCommentId: Long? = null,
    val editingText: String = "",
    val isSavingEdit: Boolean = false,
    val currentUserId: Long? = null,
    val errorMessage: String? = null,
    val needsLogin: Boolean = false
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

    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            commentRepository.getComments(quoteId)
                .onSuccess { comments ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        comments = comments,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, errorMessage = error.message)
                }
        }
    }

    fun onRefresh() = load(isRefresh = true)

    fun onDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(draftText = value)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeNeedsLogin() {
        _uiState.value = _uiState.value.copy(needsLogin = false)
    }

    fun onPostComment() {
        if (_uiState.value.currentUserId == null) {
            _uiState.value = _uiState.value.copy(needsLogin = true)
            return
        }
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

    fun onStartEdit(comment: Comment) {
        _uiState.value = _uiState.value.copy(editingCommentId = comment.id, editingText = comment.text)
    }

    fun onEditingTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(editingText = value)
    }

    fun onCancelEdit() {
        _uiState.value = _uiState.value.copy(editingCommentId = null, editingText = "")
    }

    fun onSaveEdit() {
        val id = _uiState.value.editingCommentId ?: return
        val text = _uiState.value.editingText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingEdit = true)
            commentRepository.editComment(id, text)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        isSavingEdit = false,
                        editingCommentId = null,
                        editingText = "",
                        comments = _uiState.value.comments.map { if (it.id == id) updated else it }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSavingEdit = false, errorMessage = error.message)
                }
        }
    }
}
