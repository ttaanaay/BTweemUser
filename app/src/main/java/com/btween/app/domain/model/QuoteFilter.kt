package com.btween.app.domain.model

data class QuoteFilter(
    val category: String? = null,
    val sourceType: String? = null,
    val favoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST
)
