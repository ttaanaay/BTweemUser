package com.btween.app.domain.repository

data class PublicCategory(val id: Long, val name: String)

interface PublicCategoryRepository {
    suspend fun getCategories(): Result<List<PublicCategory>>
}
