package com.btweeu.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.repository.PublicCategory
import com.btweeu.app.domain.repository.PublicCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryIconsViewModel @Inject constructor(
    private val publicCategoryRepository: PublicCategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<PublicCategory>>(emptyList())
    val categories: StateFlow<List<PublicCategory>> = _categories

    init {
        viewModelScope.launch {
            publicCategoryRepository.getCategories().onSuccess { _categories.value = it }
            // On failure, categories just stays empty and the row renders nothing - not
            // worth surfacing an error for a decorative shortcuts row on the Home screen.
        }
    }
}
