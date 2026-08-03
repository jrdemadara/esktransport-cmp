package org.noztek.esktransport.feature.common.logout.data.impl

import org.noztek.esktransport.core.notify.domain.lifecycle.PushNotificationRegistrationCoordinator
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.logout.data.remote.LogoutApi
import org.noztek.esktransport.feature.common.logout.domain.repository.LogoutRepository

class LogoutRepositoryImpl(
    private val logoutApi: LogoutApi,
    private val sessionManager: SessionManager,
    private val pushNotificationRegistrationCoordinator: PushNotificationRegistrationCoordinator,
) : LogoutRepository {
    override suspend fun logout(): Result<Unit> {
        try {
            pushNotificationRegistrationCoordinator.unregisterCurrentDevice()
            logoutApi.logout()
            sessionManager.clear()
            return Result.success(Unit)
        } catch (t: Throwable) {
            return Result.failure(t)
        }
    }
}
