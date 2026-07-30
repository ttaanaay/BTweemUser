package com.btween.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.btween.app.domain.model.QuoteCollection
import com.btween.app.domain.repository.ReportTargetType
import com.btween.app.ui.components.ReportDialog
import com.btween.app.ui.components.UserAvatar
import com.btween.app.ui.theme.QuoteSerifFontFamily
import com.btween.app.ui.util.localizedLabel
import com.btween.app.util.shareQuoteAsImage
import com.btween.app.util.shareQuoteAsText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialQuoteDetailScreen(
    onBack: () -> Unit,
    onOwnerClick: (Long) -> Unit,
    onCommentsClick: (Long) -> Unit,
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
                title = { Text("Quote") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.quote?.let { quote ->
                        Box {
                            IconButton(onClick = { showShareMenu = true }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share")
                            }
                            DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Share as text") },
                                    onClick = {
                                        showShareMenu = false
                                        shareQuoteAsText(context, quote)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share as image") },
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
                                    Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                                }
                                DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Report") },
                                        onClick = {
                                            showMoreMenu = false
                                            showReportDialog = true
                                        }
                                    )
                                }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        ) { Text("Following") }
                    } else {
                        Button(
                            onClick = viewModel::onToggleFollow,
                            enabled = !uiState.isFollowActionInFlight
                        ) { Text("Follow") }
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
                text = quote.sourceType.localizedLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        contentDescription = "Comments",
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
                        contentDescription = "Add to collection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        title = { Text("Add to collection") },
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
                        label = { Text("New collection name") },
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
            ) { Text("Create & add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
