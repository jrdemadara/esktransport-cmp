package org.noztek.esktransport.feature.common.notification.domain.usecase

import org.noztek.esktransport.feature.common.notification.domain.repository.NotificationRepository

class MarkNotificationsReadUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.markAllRead()
}
