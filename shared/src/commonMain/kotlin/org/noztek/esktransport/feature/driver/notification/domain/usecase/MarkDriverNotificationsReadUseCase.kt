package org.noztek.esktransport.feature.driver.notification.domain.usecase

import org.noztek.esktransport.feature.driver.notification.domain.repository.DriverNotificationRepository

class MarkDriverNotificationsReadUseCase(
    private val repository: DriverNotificationRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.markAllRead()
}
