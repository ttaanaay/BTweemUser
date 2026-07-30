package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.QuoteResponseDto
import com.btween.app.data.remote.dto.TopContributorResponseDto
import com.btween.app.data.remote.dto.UpdateProfileRequestDto
import com.btween.app.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): UserResponseDto

    @GET("users/top-contributors")
    suspend fun getTopContributors(@Query("limit") limit: Int = 10): List<TopContributorResponseDto>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String, @Query("limit") limit: Int = 20): List<UserResponseDto>

    @GET("users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Long = 0
    ): List<UserResponseDto>

    @GET("users/{id}/following")
    suspend fun getFollowing(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Long = 0
    ): List<UserResponseDto>

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): UserResponseDto

    @DELETE("users/me")
    suspend fun deleteAccount()

    @GET("users/{id}/quotes")
    suspend fun getUserQuotes(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Long = 0
    ): List<QuoteResponseDto>

    @POST("users/{id}/follow")
    suspend fun follow(@Path("id") id: Long)

    @DELETE("users/{id}/follow")
    suspend fun unfollow(@Path("id") id: Long)
}
