package com.btween.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btween.app.domain.model.QuoteCollection
import com.btween.app.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val collections: List<QuoteCollection> = emptyList(),
    val showCreateDialog: Boolean = false,
    val newCollectionName: String = "",
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState

    init {
        load()
    }

    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = !isRefresh, isRefreshing = isRefresh)
            collectionRepository.getCollections()
                .onSuccess { collections ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, collections = collections)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false, errorMessage = error.message)
                }
        }
    }

    fun onRefresh() = load(isRefresh = true)

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onShowCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, newCollectionName = "")
    }

    fun onDismissCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun onNewCollectionNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(newCollectionName = value)
    }

    fun onCreateCollection() {
        val name = _uiState.value.newCollectionName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            collectionRepository.createCollection(name)
                .onSuccess { collection ->
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        collections = listOf(collection) + _uiState.value.collections
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isCreating = false, errorMessage = error.message)
                }
        }
    }

    fun onDeleteCollection(id: Long) {
        val previous = _uiState.value.collections
        _uiState.value = _uiState.value.copy(collections = previous.filterNot { it.id == id })

        viewModelScope.launch {
            collectionRepository.deleteCollection(id).onFailure { error ->
                _uiState.value = _uiState.value.copy(collections = previous, errorMessage = error.message)
            }
        }
    }
}
