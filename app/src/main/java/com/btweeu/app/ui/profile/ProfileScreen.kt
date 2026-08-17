package com.btweeu.app.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.btweeu.app.R
import com.btweeu.app.domain.model.QuoteCollection
import com.btweeu.app.domain.repository.ReportTargetType
import com.btweeu.app.ui.components.EmptyState
import com.btweeu.app.ui.components.LoginRequiredDialog
import com.btweeu.app.ui.components.ReportDialog
import com.btweeu.app.ui.components.UserAvatar
import com.btweeu.app.ui.feed.SocialQuoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowListClick: (Long, FollowListType) -> Unit,
    onEditQuote: (Long) -> Unit,
    onQuoteClick: (Long) -> Unit,
    onCollectionsClick: () -> Unit,
    onCollectionDetailClick: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var quoteIdPendingDelete by remember { mutableStateOf<Long?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Reload whenever this screen comes back into view (e.g. returning from Edit Profile
    // after changing the avatar) - the ViewModel/composition survives the round trip, so
    // without this the screen would keep showing whatever it loaded the first time.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                title = { Text(uiState.user?.displayName ?: stringResource(R.string.nav_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (uiState.isOwnProfile) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings))
                        }
                    } else {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.profile_report_user)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showReportDialog = true
                                    }
                                )
                            }
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
                    if (uiState.isLoading) CircularProgressIndicator() else Text(stringResource(R.string.profile_user_not_found))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        ProfileHeader(
                            displayName = user.displayName,
                            username = user.username,
                            bio = user.bio,
                            avatarUrl = user.avatarUrl,
                            followerCount = user.followerCount,
                            followingCount = user.followingCount,
                            isOwnProfile = uiState.isOwnProfile,
                            isFollowedByMe = user.isFollowedByMe,
                            isFollowActionInFlight = uiState.isFollowActionInFlight,
                            onFollowersClick = { onFollowListClick(user.id, FollowListType.FOLLOWERS) },
                            onFollowingClick = { onFollowListClick(user.id, FollowListType.FOLLOWING) },
                            onEditProfile = onEditProfile,
                            onCollectionsClick = onCollectionsClick,
                            onToggleFollow = viewModel::onToggleFollow
                        )
                    }

                    if (uiState.isOwnProfile) {
                        item {
                            CollectionHighlightsRow(
                                collections = uiState.collections,
                                onCollectionClick = onCollectionDetailClick,
                                onNewCollectionClick = onCollectionsClick
                            )
                        }
                    }

                    if (uiState.quotes.isEmpty()) {
                        item {
                            EmptyState(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Outlined.AutoStories,
                                title = stringResource(R.string.profile_empty_quotes_title),
                                message = if (uiState.isOwnProfile) {
                                    stringResource(R.string.profile_empty_quotes_own)
                                } else {
                                    stringResource(R.string.profile_empty_quotes_other, user.displayName)
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
                                onDelete = { quoteIdPendingDelete = quote.id },
                                onQuoteClick = { onQuoteClick(quote.id) }
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
            title = { Text(stringResource(R.string.profile_delete_quote_title)) },
            text = { Text(stringResource(R.string.profile_delete_quote_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteQuote(pendingId)
                    quoteIdPendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { quoteIdPendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showReportDialog) {
        uiState.user?.let { user ->
            ReportDialog(
                targetType = ReportTargetType.USER,
                targetId = user.id,
                onDismiss = { showReportDialog = false }
            )
        }
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
private fun ProfileHeader(
    displayName: String,
    username: String,
    bio: String?,
    avatarUrl: String?,
    followerCount: Int,
    followingCount: Int,
    isOwnProfile: Boolean,
    isFollowedByMe: Boolean,
    isFollowActionInFlight: Boolean,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onEditProfile: () -> Unit,
    onCollectionsClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(
            avatarUrl = avatarUrl,
            displayName = displayName,
            size = 88.dp,
            textStyle = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(displayName, style = MaterialTheme.typography.titleLarge)
        Text(
            "@$username",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(bio, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onFollowersClick)
            ) {
                Text(followerCount.toString(), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.profile_followers),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onFollowingClick)
            ) {
                Text(followingCount.toString(), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.profile_following),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isOwnProfile) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditProfile) {
                    Text(stringResource(R.string.profile_edit_profile))
                }
                OutlinedButton(onClick = onCollectionsClick) {
                    Text(stringResource(R.string.profile_collections))
                }
            }
        } else {
            Button(onClick = onToggleFollow, enabled = !isFollowActionInFlight) {
                Text(
                    if (isFollowedByMe) {
                        stringResource(R.string.profile_following_action)
                    } else {
                        stringResource(R.string.profile_follow_action)
                    }
                )
            }
        }
    }
}

/** Instagram-Highlights-style row: each collection is a circular thumbnail with its name
 * below, horizontally scrollable. Collections have no cover image of their own, so each
 * circle uses a plain icon rather than trying to fake a thumbnail from an arbitrary quote. */
@Composable
private fun CollectionHighlightsRow(
    collections: List<QuoteCollection>,
    onCollectionClick: (Long) -> Unit,
    onNewCollectionClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable(onClick = onNewCollectionClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.profile_new_collection),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.profile_new_collection),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        items(collections, key = { it.id }) { collection ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .clickable { onCollectionClick(collection.id) }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!collection.coverImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = collection.coverImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Collections,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    collection.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
