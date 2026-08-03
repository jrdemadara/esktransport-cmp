package org.noztek.esktransport.core.notify.domain.usecase

import org.noztek.esktransport.core.notify.domain.model.PushNotificationDeviceRegistration
import org.noztek.esktransport.core.notify.domain.repository.PushNotificationRepository

class RegisterPushNotificationDeviceUseCase(
    private val repository: PushNotificationRepository,
) {
    suspend operator fun invoke(payload: PushNotificationDeviceRegistration): Result<Unit> {
        return repository.registerDevice(payload)
    }
}
