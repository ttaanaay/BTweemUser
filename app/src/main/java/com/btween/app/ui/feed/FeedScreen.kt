package com.btween.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.model.SocialQuote
import com.btween.app.domain.repository.ReportTargetType
import com.btween.app.ui.components.EmptyState
import com.btween.app.ui.components.LoginRequiredDialog
import com.btween.app.ui.components.ReportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onQuoteOwnerClick: (Long) -> Unit,
    onQuoteClick: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var reportQuoteId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Feed") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                    Tab(
                        selected = uiState.selectedTab == FeedTab.FOR_YOU,
                        onClick = { viewModel.onTabSelected(FeedTab.FOR_YOU) },
                        text = { Text("For You") }
                    )
                    Tab(
                        selected = uiState.selectedTab == FeedTab.FOLLOWING,
                        onClick = { viewModel.onTabSelected(FeedTab.FOLLOWING) },
                        text = { Text("Following") }
                    )
                }

                key(uiState.selectedTab) {
                    FeedTabContent(
                        tabState = uiState.current,
                        isFollowingTab = uiState.selectedTab == FeedTab.FOLLOWING,
                        onRefresh = viewModel::onRefresh,
                        onLoadMore = viewModel::onLoadMore,
                        onToggleLike = viewModel::onToggleLike,
                        onQuoteOwnerClick = onQuoteOwnerClick,
                        onQuoteClick = onQuoteClick,
                        onReport = { quoteId -> reportQuoteId = quoteId }
                    )
                }
            }
        }
    }

    reportQuoteId?.let { quoteId ->
        ReportDialog(
            targetType = ReportTargetType.QUOTE,
            targetId = quoteId,
            onDismiss = { reportQuoteId = null }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTabContent(
    tabState: FeedTabState,
    isFollowingTab: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: (SocialQuote) -> Unit,
    onQuoteOwnerClick: (Long) -> Unit,
    onQuoteClick: (Long) -> Unit,
    onReport: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    PullToRefreshBox(
        isRefreshing = tabState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            tabState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            tabState.quotes.isEmpty() -> {
                if (isFollowingTab) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.People,
                        title = "No quotes from people you follow yet",
                        message = "Follow more people to see their public quotes here, or check the For You tab."
                    )
                } else {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.Public,
                        title = "No public quotes yet",
                        message = "Be the first to share a quote with everyone \u2014 mark a quote as Public when adding it."
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tabState.quotes, key = { it.id }) { quote ->
                        SocialQuoteCard(
                            quote = quote,
                            onToggleLike = { onToggleLike(quote) },
                            onOwnerClick = { onQuoteOwnerClick(quote.owner.id) },
                            onQuoteClick = { onQuoteClick(quote.id) },
                            onReport = { onReport(quote.id) }
                        )
                    }
                    if (tabState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
