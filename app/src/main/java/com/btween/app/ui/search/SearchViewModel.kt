package com.btween.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.QuoteFilter
import com.btween.app.domain.model.User
import com.btween.app.domain.repository.ProfileRepository
import com.btween.app.domain.usecase.quote.GetFilteredQuotesUseCase
import com.btween.app.domain.usecase.quote.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchMode { QUOTES, PEOPLE }

data class SearchUiState(
    val mode: SearchMode = SearchMode.QUOTES,
    val query: String = "",
    val quoteResults: List<Quote> = emptyList(),
    val userResults: List<User> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val hasSearched: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getFilteredQuotesUseCase: GetFilteredQuotesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val queryState = MutableStateFlow("")
    private val modeState = MutableStateFlow(SearchMode.QUOTES)
    private val userResultsState = MutableStateFlow<List<User>>(emptyList())
    private val isSearchingUsersState = MutableStateFlow(false)
    private var userSearchJob: Job? = null

    val uiState: StateFlow<SearchUiState> = combine(
        queryState,
        modeState,
        queryState.flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else getFilteredQuotesUseCase(QuoteFilter(searchQuery = query))
        },
        userResultsState,
        isSearchingUsersState
    ) { query, mode, quoteResults, userResults, isSearchingUsers ->
        SearchUiState(
            mode = mode,
            query = query,
            quoteResults = quoteResults,
            userResults = userResults,
            isSearchingUsers = isSearchingUsers,
            hasSearched = query.isNotBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    init {
        // People search hits the network, so it's debounced separately from the reactive
        // local-quote Flow above (which is cheap enough to run on every keystroke).
        viewModelScope.launch {
            queryState.debounce(400).collect { query ->
                userSearchJob?.cancel()
                if (modeState.value != SearchMode.PEOPLE || query.isBlank()) {
                    userResultsState.value = emptyList()
                    return@collect
                }
                userSearchJob = launch { runUserSearch(query) }
            }
        }
    }

    private suspend fun runUserSearch(query: String) {
        isSearchingUsersState.value = true
        profileRepository.searchUsers(query)
            .onSuccess { userResultsState.value = it }
            .onFailure { userResultsState.value = emptyList() }
        isSearchingUsersState.value = false
    }

    fun onQueryChanged(query: String) {
        queryState.value = query
    }

    fun onModeChanged(mode: SearchMode) {
        modeState.value = mode
        if (mode == SearchMode.PEOPLE && queryState.value.isNotBlank()) {
            userSearchJob?.cancel()
            userSearchJob = viewModelScope.launch { runUserSearch(queryState.value) }
        }
    }

    fun onToggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch { toggleFavoriteUseCase(id, isFavorite) }
    }
}
