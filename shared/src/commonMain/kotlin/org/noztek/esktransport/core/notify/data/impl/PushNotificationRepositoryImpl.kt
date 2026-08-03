package org.noztek.esktransport.core.notify.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.core.notify.data.remote.PushNotificationApi
import org.noztek.esktransport.core.notify.data.remote.dto.PushDeviceDeleteRequestDto
import org.noztek.esktransport.core.notify.data.remote.dto.PushDeviceRegistrationRequestDto
import org.noztek.esktransport.core.notify.domain.model.PushNotificationDeviceRegistration
import org.noztek.esktransport.core.notify.domain.repository.PushNotificationRepository

class PushNotificationRepositoryImpl(
    private val api: PushNotificationApi,
) : PushNotificationRepository {
    override suspend fun registerDevice(payload: PushNotificationDeviceRegistration): Result<Unit> {
        return runNotifyCall("Failed to register this device for notifications.") {
            api.registerDevice(
                PushDeviceRegistrationRequestDto(
                    platform = payload.platform,
                    token = payload.token,
                    deviceId = payload.deviceId,
                    deviceName = payload.deviceName,
                    appVersion = payload.appVersion,
                    locale = payload.locale,
                    timezone = payload.timezone,
                )
            )
        }
    }

    override suspend fun unregisterDevice(token: String): Result<Unit> {
        return runNotifyCall("Failed to remove this device from notifications.") {
            api.unregisterDevice(PushDeviceDeleteRequestDto(token = token))
        }
    }

    private suspend fun runNotifyCall(
        fallbackMessage: String,
        block: suspend () -> Unit,
    ): Result<Unit> {
        return try {
            block()
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, fallbackMessage)
            Result.failure(IllegalStateException(message))
        }
    }
}
