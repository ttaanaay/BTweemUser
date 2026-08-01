package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.CategoryResponseDto
import retrofit2.http.GET

interface CategoryApi {

    @GET("categories")
    suspend fun getCategories(): List<CategoryResponseDto>
}
