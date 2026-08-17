package com.btweeu.app.domain.repository

data class PublicCategory(val id: Long, val name: String, val icon: String)

interface PublicCategoryRepository {
    suspend fun getCategories(): Result<List<PublicCategory>>
}
