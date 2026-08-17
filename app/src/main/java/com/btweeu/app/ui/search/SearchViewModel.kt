package com.btweeu.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.model.SocialQuote
import com.btweeu.app.domain.model.User
import com.btweeu.app.domain.repository.ProfileRepository
import com.btweeu.app.domain.repository.SocialQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchMode { QUOTES, PEOPLE }

data class SearchUiState(
    val mode: SearchMode = SearchMode.QUOTES,
    val query: String = "",
    val quoteResults: List<SocialQuote> = emptyList(),
    val userResults: List<User> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val isSearchingQuotes: Boolean = false,
    val hasSearched: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val socialQuoteRepository: SocialQuoteRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val queryState = MutableStateFlow("")
    private val modeState = MutableStateFlow(SearchMode.QUOTES)
    private val userResultsState = MutableStateFlow<List<User>>(emptyList())
    private val isSearchingUsersState = MutableStateFlow(false)
    private val quoteResultsState = MutableStateFlow<List<SocialQuote>>(emptyList())
    private val isSearchingQuotesState = MutableStateFlow(false)
    private var userSearchJob: Job? = null
    private var quoteSearchJob: Job? = null

    val uiState: StateFlow<SearchUiState> = combine(
        combine(queryState, modeState) { query, mode -> query to mode },
        combine(quoteResultsState, isSearchingQuotesState) { quotes, isSearching -> quotes to isSearching },
        combine(userResultsState, isSearchingUsersState) { users, isSearching -> users to isSearching }
    ) { (query, mode), (quoteResults, isSearchingQuotes), (userResults, isSearchingUsers) ->
        SearchUiState(
            mode = mode,
            query = query,
            quoteResults = quoteResults,
            userResults = userResults,
            isSearchingUsers = isSearchingUsers,
            isSearchingQuotes = isSearchingQuotes,
            hasSearched = query.isNotBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    init {
        // Both searches hit the network, so debounce rather than firing on every keystroke.
        viewModelScope.launch {
            queryState.debounce(400).collect { query ->
                userSearchJob?.cancel()
                quoteSearchJob?.cancel()
                if (query.isBlank()) {
                    userResultsState.value = emptyList()
                    quoteResultsState.value = emptyList()
                    return@collect
                }
                if (modeState.value == SearchMode.PEOPLE) {
                    userSearchJob = launch { runUserSearch(query) }
                } else {
                    quoteSearchJob = launch { runQuoteSearch(query) }
                }
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

    /** No dedicated quote-search endpoint exists yet - filters over a batch of recent public
     * quotes client-side instead, matching the same approach used on the web app. Only
     * searches what's in that batch, not every quote ever posted. */
    private suspend fun runQuoteSearch(query: String) {
        isSearchingQuotesState.value = true
        socialQuoteRepository.getFeed(limit = 50)
            .onSuccess { quotes ->
                val needle = query.trim().lowercase()
                quoteResultsState.value = quotes.filter { quote ->
                    quote.text.lowercase().contains(needle) ||
                        quote.speaker.lowercase().contains(needle) ||
                        quote.sourceTitle.lowercase().contains(needle) ||
                        quote.author?.lowercase()?.contains(needle) == true
                }
            }
            .onFailure { quoteResultsState.value = emptyList() }
        isSearchingQuotesState.value = false
    }

    fun onQueryChanged(query: String) {
        queryState.value = query
    }

    fun onModeChanged(mode: SearchMode) {
        modeState.value = mode
        val query = queryState.value
        if (query.isBlank()) return
        if (mode == SearchMode.PEOPLE) {
            userSearchJob?.cancel()
            userSearchJob = viewModelScope.launch { runUserSearch(query) }
        } else {
            quoteSearchJob?.cancel()
            quoteSearchJob = viewModelScope.launch { runQuoteSearch(query) }
        }
    }

    fun onToggleLike(quote: SocialQuote) {
        val optimistic = quote.copy(
            isLikedByMe = !quote.isLikedByMe,
            likeCount = if (quote.isLikedByMe) quote.likeCount - 1 else quote.likeCount + 1
        )
        quoteResultsState.value = quoteResultsState.value.map { if (it.id == quote.id) optimistic else it }

        viewModelScope.launch {
            val result = if (optimistic.isLikedByMe) {
                socialQuoteRepository.likeQuote(quote.id)
            } else {
                socialQuoteRepository.unlikeQuote(quote.id)
            }
            result.onFailure {
                quoteResultsState.value = quoteResultsState.value.map { q -> if (q.id == quote.id) quote else q }
            }
        }
    }
}
