package org.noztek.esktransport.feature.driver.notification.domain.repository

import org.noztek.esktransport.feature.driver.notification.domain.model.DriverNotificationsPage

interface DriverNotificationRepository {
    suspend fun getNotifications(): Result<DriverNotificationsPage>
    suspend fun markAllRead(): Result<Unit>
}
