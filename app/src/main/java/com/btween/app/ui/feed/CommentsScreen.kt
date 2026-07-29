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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.model.Comment
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    onBack: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.comments.isEmpty() -> {
                EmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = "No comments yet",
                    message = "Be the first to say something about this quote."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.comments, key = { it.id }) { comment ->
                        CommentRow(
                            comment = comment,
                            canDelete = comment.author.id == uiState.currentUserId,
                            onDelete = { viewModel.onDeleteComment(comment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment, canDelete: Boolean, onDelete: () -> Unit) {
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
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
