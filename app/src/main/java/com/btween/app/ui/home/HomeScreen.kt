package com.btween.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.R
import com.btween.app.domain.model.SocialQuote
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.components.SectionHeader
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddQuote: () -> Unit,
    onSearch: () -> Unit,
    onUserClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_search))
                    }
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationCount > 0) {
                                    Badge {
                                        Text(
                                            if (uiState.unreadNotificationCount > 9) "9+"
                                            else uiState.unreadNotificationCount.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddQuote,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.action_add_quote)) }
            )
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.isEmpty -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.AutoStories,
                        title = stringResource(R.string.home_empty_title),
                        message = stringResource(R.string.home_empty_message),
                        actionLabel = stringResource(R.string.home_empty_action),
                        onAction = onAddQuote
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        uiState.dailyQuote?.let { daily ->
                            item {
                                HeroQuoteCard(
                                    quote = daily,
                                    onToggleLike = { viewModel.onToggleLike(daily) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    onQuoteClick = { onQuoteClick(daily.id) }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        if (uiState.trending.isNotEmpty()) {
                            item {
                                SectionHeader(title = stringResource(R.string.home_section_trending))
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(uiState.trending, key = { it.id }) { quote ->
                                        CompactGradientQuoteCard(
                                            quote = quote,
                                            onToggleLike = { viewModel.onToggleLike(quote) },
                                            modifier = Modifier.width(240.dp),
                                            onQuoteClick = { onQuoteClick(quote.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        item {
                            SectionHeader(title = stringResource(R.string.home_section_categories))
                            Spacer(modifier = Modifier.height(8.dp))
                            CategoryIconsRow()
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (uiState.recentlyApproved.isNotEmpty()) {
                            item {
                                SectionHeader(title = stringResource(R.string.home_section_recently_approved))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            item {
                                RecentlyApprovedGrid(
                                    quotes = uiState.recentlyApproved,
                                    onToggleLike = viewModel::onToggleLike,
                                    onQuoteClick = onQuoteClick
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        if (uiState.topContributors.isNotEmpty()) {
                            item {
                                SectionHeader(title = stringResource(R.string.home_section_top_contributors))
                                Spacer(modifier = Modifier.height(8.dp))
                                TopContributorsRow(
                                    contributors = uiState.topContributors,
                                    onContributorClick = onUserClick
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyApprovedGrid(
    quotes: List<SocialQuote>,
    onToggleLike: (SocialQuote) -> Unit
) {
    // A non-scrolling grid nested inside the outer LazyColumn: height is capped and derived
    // from content, avoiding the "infinite height" crash a nested LazyVerticalGrid would
    // otherwise cause inside a LazyColumn.
    val rows = quotes.chunked(2)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowQuotes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowQuotes.forEach { quote ->
                    CompactGradientQuoteCard(
                        quote = quote,
                        onToggleLike = { onToggleLike(quote) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowQuotes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
