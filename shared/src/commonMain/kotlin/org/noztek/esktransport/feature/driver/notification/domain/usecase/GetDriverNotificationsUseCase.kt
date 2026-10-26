package org.noztek.esktransport.feature.driver.notification.domain.usecase

import org.noztek.esktransport.feature.driver.notification.domain.model.DriverNotificationsPage
import org.noztek.esktransport.feature.driver.notification.domain.repository.DriverNotificationRepository

class GetDriverNotificationsUseCase(
    private val repository: DriverNotificationRepository,
) {
    suspend operator fun invoke(): Result<DriverNotificationsPage> = repository.getNotifications()
}
