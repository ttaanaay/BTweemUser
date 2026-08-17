package com.btweeu.app.ui.collections

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.domain.model.QuoteCollection
import com.btweeu.app.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onBack: () -> Unit,
    onCollectionClick: (Long) -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var collectionIdPendingDelete by remember { mutableStateOf<Long?>(null) }
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
                title = { Text(stringResource(R.string.collections_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onShowCreateDialog) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.collections_action_new))
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.collections.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.CollectionsBookmark,
                        title = stringResource(R.string.collections_empty_title),
                        message = stringResource(R.string.collections_empty_message)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.collections, key = { it.id }) { collection ->
                            CollectionRow(
                                collection = collection,
                                onClick = { onCollectionClick(collection.id) },
                                onDelete = { collectionIdPendingDelete = collection.id }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissCreateDialog,
            title = { Text(stringResource(R.string.collections_new_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.newCollectionName,
                    onValueChange = viewModel::onNewCollectionNameChanged,
                    label = { Text(stringResource(R.string.collections_label_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onCreateCollection,
                    enabled = !uiState.isCreating && uiState.newCollectionName.isNotBlank()
                ) { Text(stringResource(R.string.collections_action_create)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissCreateDialog) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    collectionIdPendingDelete?.let { pendingId ->
        AlertDialog(
            onDismissRequest = { collectionIdPendingDelete = null },
            title = { Text(stringResource(R.string.collections_delete_title)) },
            text = { Text(stringResource(R.string.collections_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteCollection(pendingId)
                    collectionIdPendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { collectionIdPendingDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun CollectionRow(collection: QuoteCollection, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CollectionsBookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(collection.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${collection.quoteCount} quotes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
