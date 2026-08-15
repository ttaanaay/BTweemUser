package com.btween.app.data.repository

import com.btween.app.data.remote.api.CategoryApi
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.repository.PublicCategory
import com.btween.app.domain.repository.PublicCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicCategoryRepositoryImpl @Inject constructor(
    private val categoryApi: CategoryApi
) : PublicCategoryRepository {

    override suspend fun getCategories(): Result<List<PublicCategory>> = safeApiCall {
        categoryApi.getCategories().map { PublicCategory(it.id, it.name, it.icon) }
    }
}
