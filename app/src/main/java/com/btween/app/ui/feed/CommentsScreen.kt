package com.btween.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.model.Comment
import com.btween.app.domain.repository.ReportTargetType
import com.btween.app.ui.components.LoginRequiredDialog
import com.btween.app.ui.components.ReportDialog
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var reportCommentId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.draftText,
                    onValueChange = viewModel::onDraftChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    maxLines = 4
                )
                IconButton(
                    onClick = viewModel::onPostComment,
                    enabled = !uiState.isPosting && uiState.draftText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post comment")
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.comments.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "No comments yet",
                        message = "Be the first to say something about this quote."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.comments, key = { it.id }) { comment ->
                            CommentRow(
                                comment = comment,
                                canModify = comment.author.id == uiState.currentUserId,
                                isEditing = uiState.editingCommentId == comment.id,
                                editingText = uiState.editingText,
                                isSavingEdit = uiState.isSavingEdit,
                                onEditingTextChanged = viewModel::onEditingTextChanged,
                                onStartEdit = { viewModel.onStartEdit(comment) },
                                onCancelEdit = viewModel::onCancelEdit,
                                onSaveEdit = viewModel::onSaveEdit,
                                onDelete = { viewModel.onDeleteComment(comment.id) },
                                onReport = { reportCommentId = comment.id }
                            )
                        }
                    }
                }
            }
        }
    }

    reportCommentId?.let { commentId ->
        ReportDialog(
            targetType = ReportTargetType.COMMENT,
            targetId = commentId,
            onDismiss = { reportCommentId = null }
        )
    }

    if (uiState.needsLogin) {
        LoginRequiredDialog(
            onDismiss = viewModel::consumeNeedsLogin,
            onLogIn = {
                viewModel.consumeNeedsLogin()
                onNavigateToLogin()
            }
        )
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    canModify: Boolean,
    isEditing: Boolean,
    editingText: String,
    isSavingEdit: Boolean,
    onEditingTextChanged: (String) -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            avatarUrl = comment.author.avatarUrl,
            displayName = comment.author.displayName,
            size = 36.dp
        )
        Spacer(modifier = Modifier.padding(start = 10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(comment.author.displayName, style = MaterialTheme.typography.labelLarge)

            if (isEditing) {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                Row {
                    IconButton(onClick = onSaveEdit, enabled = !isSavingEdit && editingText.isNotBlank()) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                    IconButton(onClick = onCancelEdit, enabled = !isSavingEdit) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                    }
                }
            } else {
                Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                if (comment.isEdited) {
                    Text(
                        "(edited)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (canModify && !isEditing) {
            IconButton(onClick = onStartEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (!canModify) {
            IconButton(onClick = onReport) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = "Report comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
