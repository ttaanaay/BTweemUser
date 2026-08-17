package com.btweeu.app.data.repository

import com.btweeu.app.data.remote.api.NotificationApi
import com.btweeu.app.data.remote.safeApiCall
import com.btweeu.app.domain.model.Notification
import com.btweeu.app.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(limit: Int, offset: Long): Result<List<Notification>> = safeApiCall {
        notificationApi.getNotifications(limit, offset).map {
            Notification(
                id = it.id,
                type = it.type,
                actorId = it.actorId,
                actorUsername = it.actorUsername,
                actorDisplayName = it.actorDisplayName,
                quoteId = it.quoteId,
                quoteTextPreview = it.quoteTextPreview,
                isRead = it.isRead,
                createdAt = it.createdAt
            )
        }
    }

    override suspend fun getUnreadCount(): Result<Long> = safeApiCall {
        notificationApi.getUnreadCount().count
    }

    override suspend fun markRead(id: Long): Result<Unit> = safeApiCall {
        notificationApi.markRead(id)
    }

    override suspend fun markAllRead(): Result<Unit> = safeApiCall {
        notificationApi.markAllRead()
    }
}
