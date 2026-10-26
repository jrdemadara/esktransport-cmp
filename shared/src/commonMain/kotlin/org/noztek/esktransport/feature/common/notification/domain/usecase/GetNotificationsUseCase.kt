package org.noztek.esktransport.feature.common.notification.domain.usecase

import org.noztek.esktransport.feature.common.notification.domain.model.NotificationsPage
import org.noztek.esktransport.feature.common.notification.domain.repository.NotificationRepository

class GetNotificationsUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<NotificationsPage> = repository.getNotifications()
}
