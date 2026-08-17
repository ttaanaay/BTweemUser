package com.btweeu.app.domain.repository

import com.btweeu.app.domain.model.Notification

interface NotificationRepository {

    suspend fun getNotifications(limit: Int = 20, offset: Long = 0): Result<List<Notification>>

    suspend fun getUnreadCount(): Result<Long>

    suspend fun markRead(id: Long): Result<Unit>

    suspend fun markAllRead(): Result<Unit>
}
