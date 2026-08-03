package org.noztek.esktransport.core.notify.domain.repository

import org.noztek.esktransport.core.notify.domain.model.PushNotificationDeviceRegistration

interface PushNotificationRepository {
    suspend fun registerDevice(payload: PushNotificationDeviceRegistration): Result<Unit>
    suspend fun unregisterDevice(token: String): Result<Unit>
}
