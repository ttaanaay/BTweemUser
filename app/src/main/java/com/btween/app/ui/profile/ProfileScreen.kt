package com.btween.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.feed.SocialQuoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowListClick: (Long, FollowListType) -> Unit,
    onEditQuote: (Long) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var quoteIdPendingDelete by remember { mutableStateOf<Long?>(null) }

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
                title = { Text(uiState.user?.displayName ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isOwnProfile) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val user = uiState.user

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading || user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) CircularProgressIndicator() else Text("User not found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(user.displayName, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "@${user.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!user.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    user.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        onFollowListClick(user.id, FollowListType.FOLLOWERS)
                                    }
                                ) {
                                    Text(user.followerCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Followers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        onFollowListClick(user.id, FollowListType.FOLLOWING)
                                    }
                                ) {
                                    Text(user.followingCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Following",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.isOwnProfile) {
                                OutlinedButton(onClick = onEditProfile) {
                                    Text("Edit profile")
                                }
                            } else {
                                Button(
                                    onClick = viewModel::onToggleFollow,
                                    enabled = !uiState.isFollowActionInFlight
                                ) {
                                    Text(if (user.isFollowedByMe) "Following" else "Follow")
                                }
                            }
                        }
                    }

                    if (uiState.quotes.isEmpty()) {
                        item {
                            EmptyState(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Outlined.AutoStories,
                                title = "No quotes yet",
                                message = if (uiState.isOwnProfile) {
                                    "Quotes you share publicly will show up here."
                                } else {
                                    "${user.displayName} hasn't shared any quotes yet."
                                }
                            )
                        }
                    } else {
                        items(uiState.quotes, key = { it.id }) { quote ->
                            SocialQuoteCard(
                                quote = quote,
                                onToggleLike = { viewModel.onToggleLike(quote) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                showOwnerActions = uiState.isOwnProfile,
                                onEdit = { onEditQuote(quote.id) },
                                onDelete = { quoteIdPendingDelete = quote.id }
                            )
                        }
                    }
                }
            }
        }
    }

    quoteIdPendingDelete?.let { pendingId ->
        AlertDialog(
            onDismissRequest = { quoteIdPendingDelete = null },
            title = { Text("Delete quote?") },
            text = { Text("This will remove it from the feed for everyone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteQuote(pendingId)
                    quoteIdPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { quoteIdPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}
