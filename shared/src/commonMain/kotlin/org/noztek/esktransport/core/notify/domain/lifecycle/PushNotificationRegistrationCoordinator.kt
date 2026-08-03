package org.noztek.esktransport.core.notify.domain.lifecycle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.notify.PushNotificationTokenProvider
import org.noztek.esktransport.core.notify.pushNotificationPlatform
import org.noztek.esktransport.core.notify.domain.model.PushNotificationDeviceRegistration
import org.noztek.esktransport.core.notify.domain.usecase.RegisterPushNotificationDeviceUseCase
import org.noztek.esktransport.core.notify.domain.usecase.UnregisterPushNotificationDeviceUseCase
import org.noztek.esktransport.core.platform.AppBuildInfo

class PushNotificationRegistrationCoordinator(
    private val tokenProvider: PushNotificationTokenProvider,
    private val registerUseCase: RegisterPushNotificationDeviceUseCase,
    private val unregisterUseCase: UnregisterPushNotificationDeviceUseCase,
    private val appBuildInfo: AppBuildInfo,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private var lastRegisteredToken: String? = null

    suspend fun registerCurrentDevice(): Result<Unit> {
        val token = tokenProvider.currentToken().getOrElse { return Result.failure(it) }
        return registerToken(token)
    }

    suspend fun registerToken(token: String): Result<Unit> {
        if (token.isBlank()) return Result.failure(IllegalArgumentException("Push token is blank."))

        if (lastRegisteredToken == token) return Result.success(Unit)

        val result = withContext(ioDispatcher) {
            registerUseCase(
                PushNotificationDeviceRegistration(
                    platform = pushNotificationPlatform(),
                    token = token,
                    appVersion = appBuildInfo.versionName,
                )
            )
        }

        if (result.isSuccess) {
            lastRegisteredToken = token
        }
        return result
    }

    suspend fun unregisterCurrentDevice(): Result<Unit> {
        val token = lastRegisteredToken
            ?: tokenProvider.currentToken().getOrNull()
            ?: return Result.success(Unit)

        val result = withContext(ioDispatcher) {
            unregisterUseCase(token)
        }

        if (result.isSuccess) {
            lastRegisteredToken = null
        }
        return result
    }
}
