package com.btweeu.app.data.remote.api

import com.btweeu.app.data.remote.dto.CommentRequestDto
import com.btweeu.app.data.remote.dto.CommentResponseDto
import com.btweeu.app.data.remote.dto.QuoteRequestDto
import com.btweeu.app.data.remote.dto.QuoteResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuoteApi {

    @GET("quotes/feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Long = 0,
        @Query("scope") scope: String = "recommended",
        @Query("category") category: String? = null
    ): List<QuoteResponseDto>

    @GET("quotes/by-tag")
    suspend fun getQuotesByTag(
        @Query("tag") tag: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Long = 0
    ): List<QuoteResponseDto>

    @GET("quotes/{id}")
    suspend fun getQuote(@Path("id") id: Long): QuoteResponseDto

    @GET("quotes/daily")
    suspend fun getDailyQuote(): QuoteResponseDto

    @POST("quotes")
    suspend fun createQuote(@Body request: QuoteRequestDto): QuoteResponseDto

    @PUT("quotes/{id}")
    suspend fun updateQuote(@Path("id") id: Long, @Body request: QuoteRequestDto): QuoteResponseDto

    @DELETE("quotes/{id}")
    suspend fun deleteQuote(@Path("id") id: Long)

    @POST("quotes/{id}/like")
    suspend fun likeQuote(@Path("id") id: Long)

    @DELETE("quotes/{id}/like")
    suspend fun unlikeQuote(@Path("id") id: Long)

    @GET("quotes/{id}/comments")
    suspend fun getComments(
        @Path("id") quoteId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Long = 0
    ): List<CommentResponseDto>

    @POST("quotes/{id}/comments")
    suspend fun addComment(@Path("id") quoteId: Long, @Body request: CommentRequestDto): CommentResponseDto

    @PUT("comments/{id}")
    suspend fun editComment(@Path("id") id: Long, @Body request: CommentRequestDto): CommentResponseDto

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: Long)
}
