package org.noztek.esktransport.feature.common.notification.domain.repository

import org.noztek.esktransport.feature.common.notification.domain.model.NotificationsPage

interface NotificationRepository {
    suspend fun getNotifications(): Result<NotificationsPage>
    suspend fun markAllRead(): Result<Unit>
}
