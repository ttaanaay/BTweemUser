package com.btweeu.app.data.remote.api

import com.btweeu.app.data.remote.dto.NotificationDto
import com.btweeu.app.data.remote.dto.UnreadCountDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Long = 0
    ): List<NotificationDto>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountDto

    @POST("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: Long)

    @POST("notifications/read-all")
    suspend fun markAllRead()
}
