package com.btween.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.btween.app.R
import com.btween.app.domain.model.QuoteCollection
import com.btween.app.domain.repository.ReportTargetType
import com.btween.app.ui.components.LoginRequiredDialog
import com.btween.app.ui.components.ReportDialog
import com.btween.app.ui.components.UserAvatar
import com.btween.app.ui.theme.QuoteSerifFontFamily
import com.btween.app.ui.util.sourceTypeLabel
import com.btween.app.util.shareQuoteAsImage
import com.btween.app.util.shareQuoteAsText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SocialQuoteDetailScreen(
    onBack: () -> Unit,
    onOwnerClick: (Long) -> Unit,
    onCommentsClick: (Long) -> Unit,
    onEditQuote: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onTagClick: (String) -> Unit,
    viewModel: SocialQuoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showShareMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeInfo()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quote_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    uiState.quote?.let { quote ->
                        Box {
                            IconButton(onClick = { showShareMenu = true }) {
                                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.quote_detail_share))
                            }
                            DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quote_detail_share_text)) },
                                    onClick = {
                                        showShareMenu = false
                                        shareQuoteAsText(context, quote)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quote_detail_share_image)) },
                                    onClick = {
                                        showShareMenu = false
                                        scope.launch { shareQuoteAsImage(context, quote) }
                                    }
                                )
                            }
                        }
                        if (!uiState.isOwnQuote) {
                            var showMoreMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.quote_detail_more_options))
                                }
                                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.quote_detail_report)) },
                                        onClick = {
                                            showMoreMenu = false
                                            showReportDialog = true
                                        }
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = { onEditQuote(quote.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.quote_detail_edit))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.quote == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val quote = uiState.quote!!

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOwnerClick(quote.owner.id) }
            ) {
                UserAvatar(
                    avatarUrl = quote.owner.avatarUrl,
                    displayName = quote.owner.displayName,
                    size = 44.dp
                )
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(quote.owner.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "@${quote.owner.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!uiState.isOwnQuote) {
                    if (quote.owner.isFollowedByMe) {
                        OutlinedButton(
                            onClick = viewModel::onToggleFollow,
                            enabled = !uiState.isFollowActionInFlight
                        ) { Text(stringResource(R.string.onboarding_following_action)) }
                    } else {
                        Button(
                            onClick = viewModel::onToggleFollow,
                            enabled = !uiState.isFollowActionInFlight
                        ) { Text(stringResource(R.string.onboarding_follow_action)) }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 24.dp))

            if (!quote.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = quote.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.padding(top = 20.dp))
            }

            Text(
                text = "\u201C${quote.text}\u201D",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = QuoteSerifFontFamily,
                    fontStyle = FontStyle.Italic
                )
            )

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "${quote.speaker} \u2014 ${quote.sourceTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sourceTypeLabel(quote.sourceType),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (quote.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(top = 12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quote.tags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagClick(tag) },
                            label = { Text("#$tag", style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::onToggleLike) {
                    Icon(
                        imageVector = if (quote.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (quote.isLikedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    quote.likeCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.padding(start = 16.dp))

                IconButton(onClick = { onCommentsClick(quote.id) }) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.quote_detail_comments),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    quote.commentCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.padding(start = 16.dp))

                IconButton(onClick = viewModel::onShowCollectionPicker) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(R.string.quote_detail_add_to_collection),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }
    }

    if (uiState.showCollectionPicker) {
        AddToCollectionDialog(
            collections = uiState.collections,
            isLoading = uiState.isLoadingCollections,
            onDismiss = viewModel::onDismissCollectionPicker,
            onPick = viewModel::onAddToCollection,
            onCreateNew = viewModel::onCreateCollectionAndAdd
        )
    }

    if (showReportDialog) {
        uiState.quote?.let { quote ->
            ReportDialog(
                targetType = ReportTargetType.QUOTE,
                targetId = quote.id,
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
private fun AddToCollectionDialog(
    collections: List<QuoteCollection>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
    onCreateNew: (String) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quote_detail_new_collection_title)) },
        text = {
            Column {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    collections.forEach { collection ->
                        Text(
                            collection.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(collection.id) }
                                .padding(vertical = 12.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.quote_detail_new_collection_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateNew(newName) },
                enabled = newName.isNotBlank()
            ) { Text(stringResource(R.string.quote_detail_create_and_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
