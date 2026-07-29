package com.btween.app.ui.search

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.R
import com.btween.app.domain.model.User
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.components.QuoteListCard
import com.btween.app.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    onUserClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.mode.ordinal) {
                Tab(
                    selected = uiState.mode == SearchMode.QUOTES,
                    onClick = { viewModel.onModeChanged(SearchMode.QUOTES) },
                    text = { Text("Quotes") }
                )
                Tab(
                    selected = uiState.mode == SearchMode.PEOPLE,
                    onClick = { viewModel.onModeChanged(SearchMode.PEOPLE) },
                    text = { Text("People") }
                )
            }

            when (uiState.mode) {
                SearchMode.QUOTES -> QuoteResults(uiState, onQuoteClick, viewModel)
                SearchMode.PEOPLE -> PeopleResults(uiState, onUserClick)
            }
        }
    }
}

@Composable
private fun QuoteResults(
    uiState: SearchUiState,
    onQuoteClick: (Long) -> Unit,
    viewModel: SearchViewModel
) {
    when {
        !uiState.hasSearched -> EmptyState(
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Filled.Search,
            title = stringResource(R.string.search_empty_title),
            message = stringResource(R.string.search_empty_message)
        )
        uiState.quoteResults.isEmpty() -> EmptyState(
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Filled.Search,
            title = stringResource(R.string.search_no_results_title),
            message = stringResource(R.string.search_no_results_message)
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.quoteResults, key = { it.id }) { quote ->
                QuoteListCard(
                    quote = quote,
                    onClick = { onQuoteClick(quote.id) },
                    onToggleFavorite = {
                        viewModel.onToggleFavorite(quote.id, !quote.isFavorite)
                    }
                )
            }
        }
    }
}

@Composable
private fun PeopleResults(
    uiState: SearchUiState,
    onUserClick: (Long) -> Unit
) {
    when {
        !uiState.hasSearched -> EmptyState(
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Outlined.Person,
            title = "Find people",
            message = "Search by username or display name."
        )
        uiState.isSearchingUsers -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.userResults.isEmpty() -> EmptyState(
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Outlined.Person,
            title = "No one found",
            message = "Try a different username or name."
        )
        else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(uiState.userResults, key = { it.id }) { user ->
                UserRow(user = user, onClick = { onUserClick(user.id) })
            }
        }
    }
}

@Composable
private fun UserRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            avatarUrl = user.avatarUrl,
            displayName = user.displayName,
            size = 44.dp
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column {
            Text(user.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
