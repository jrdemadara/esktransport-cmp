package org.noztek.esktransport.feature.driver.notification.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.notification.data.remote.DriverNotificationApi
import org.noztek.esktransport.feature.driver.notification.data.remote.dto.DriverNotificationDto
import org.noztek.esktransport.feature.driver.notification.domain.model.DriverNotification
import org.noztek.esktransport.feature.driver.notification.domain.model.DriverNotificationsPage
import org.noztek.esktransport.feature.driver.notification.domain.repository.DriverNotificationRepository

class DriverNotificationRepositoryImpl(
    private val api: DriverNotificationApi,
) : DriverNotificationRepository {
    override suspend fun getNotifications(): Result<DriverNotificationsPage> {
        return try {
            val response = api.getNotifications()
            Result.success(
                DriverNotificationsPage(
                    notifications = response.data.map(DriverNotificationDto::toDomain),
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

private fun DriverNotificationDto.toDomain(): DriverNotification {
    return DriverNotification(
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
