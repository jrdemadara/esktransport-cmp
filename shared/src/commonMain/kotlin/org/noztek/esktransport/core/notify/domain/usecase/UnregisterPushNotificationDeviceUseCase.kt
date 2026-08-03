package org.noztek.esktransport.core.notify.domain.usecase

import org.noztek.esktransport.core.notify.domain.repository.PushNotificationRepository

class UnregisterPushNotificationDeviceUseCase(
    private val repository: PushNotificationRepository,
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return repository.unregisterDevice(token)
    }
}
