package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.AddItemRequestDto
import com.btween.app.data.remote.dto.CollectionDetailResponseDto
import com.btween.app.data.remote.dto.CollectionRequestDto
import com.btween.app.data.remote.dto.CollectionResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CollectionApi {

    @GET("collections")
    suspend fun getCollections(): List<CollectionResponseDto>

    @POST("collections")
    suspend fun createCollection(@Body request: CollectionRequestDto): CollectionResponseDto

    @GET("collections/{id}")
    suspend fun getCollection(@Path("id") id: Long): CollectionDetailResponseDto

    @DELETE("collections/{id}")
    suspend fun deleteCollection(@Path("id") id: Long)

    @POST("collections/{id}/items")
    suspend fun addItem(@Path("id") id: Long, @Body request: AddItemRequestDto)

    @DELETE("collections/{id}/items/{quoteId}")
    suspend fun removeItem(@Path("id") id: Long, @Path("quoteId") quoteId: Long)
}
