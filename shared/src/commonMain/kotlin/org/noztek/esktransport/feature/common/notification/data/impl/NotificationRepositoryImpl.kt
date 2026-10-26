package org.noztek.esktransport.feature.common.notification.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.notification.data.remote.NotificationApi
import org.noztek.esktransport.feature.common.notification.data.remote.dto.NotificationDto
import org.noztek.esktransport.feature.common.notification.domain.model.Notification
import org.noztek.esktransport.feature.common.notification.domain.model.NotificationsPage
import org.noztek.esktransport.feature.common.notification.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val api: NotificationApi,
) : NotificationRepository {
    override suspend fun getNotifications(): Result<NotificationsPage> {
        return try {
            val response = api.getNotifications()
            Result.success(
                NotificationsPage(
                    notifications = response.data.map(NotificationDto::toDomain),
                    unreadCount = response.unreadCount,
                ),
            )
        } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, "Failed to load notifications.")))
        }
    }

    override suspend fun markAllRead(): Result<Unit> {
        return try {
            api.markAllRead()
            Result.success(Unit)
        } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, "Failed to mark notifications as read.")))
        }
    }
}

private fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        type = type,
        title = title,
        body = body,
        status = status,
        createdAt = createdAt,
        sentAt = sentAt,
        readAt = readAt,
    )
}
