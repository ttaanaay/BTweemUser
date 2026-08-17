package com.btweeu.app.data.repository

import com.btweeu.app.data.remote.api.CategoryApi
import com.btweeu.app.data.remote.safeApiCall
import com.btweeu.app.domain.repository.PublicCategory
import com.btweeu.app.domain.repository.PublicCategoryRepository
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
