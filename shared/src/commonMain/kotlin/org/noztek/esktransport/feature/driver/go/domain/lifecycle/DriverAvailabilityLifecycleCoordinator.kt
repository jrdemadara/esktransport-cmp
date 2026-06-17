package org.noztek.esktransport.feature.driver.go.domain.lifecycle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.driver.go.domain.usecase.SetDriverAvailabilityUseCase

class DriverAvailabilityLifecycleCoordinator(
    private val sessionManager: SessionManager,
    private val setDriverAvailabilityUseCase: SetDriverAvailabilityUseCase,
    ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var isUpdatingOffline = false

    fun markOfflineOnAppBackground() {
        if (isUpdatingOffline) return
        scope.launch {
            if (!isCurrentUserDriver()) return@launch
            isUpdatingOffline = true
            try {
                setDriverAvailabilityUseCase(isAvailable = false)
            } finally {
                isUpdatingOffline = false
            }
        }
    }

    private suspend fun isCurrentUserDriver(): Boolean {
        val role = sessionManager.userRole.first()?.trim()?.lowercase()
        val roles = sessionManager.userRoles.first()
        return role == "driver" || roles.contains("driver")
    }
}
