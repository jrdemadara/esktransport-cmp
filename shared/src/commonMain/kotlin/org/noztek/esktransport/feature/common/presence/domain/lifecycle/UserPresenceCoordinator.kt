package org.noztek.esktransport.feature.common.presence.domain.lifecycle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.common.presence.domain.usecase.MarkUserForegroundUseCase
import org.noztek.esktransport.feature.common.presence.domain.usecase.MarkUserOfflineUseCase

class UserPresenceCoordinator(
    private val sessionManager: SessionManager,
    private val markUserForegroundUseCase: MarkUserForegroundUseCase,
    private val markUserOfflineUseCase: MarkUserOfflineUseCase,
    ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var currentRole: UserPresenceRole? = null
    private var currentContext: UserPresenceContext? = null
    private var isForeground = false

    fun markForeground(
        role: UserPresenceRole? = currentRole,
        context: UserPresenceContext? = currentContext,
    ) {
        currentRole = role
        currentContext = context
        isForeground = true
        scope.launch {
            if (!isAuthenticated()) return@launch
            markUserForegroundUseCase(currentRole, currentContext).onFailure { error ->
                println("User presence foreground failed: ${error.message}")
            }
        }
    }

    fun markOffline() {
        isForeground = false
        scope.launch {
            if (!isAuthenticated()) return@launch
            markUserOfflineUseCase(currentRole, currentContext).onFailure { error ->
                println("User presence offline failed: ${error.message}")
            }
        }
    }

    fun updateContext(
        role: UserPresenceRole,
        context: UserPresenceContext,
        metadata: Map<String, String> = emptyMap(),
    ) {
        currentRole = role
        currentContext = context
        if (!isForeground) return

        scope.launch {
            if (!isAuthenticated()) return@launch
            markUserForegroundUseCase(role, context, metadata).onFailure { error ->
                println("User presence context update failed: ${error.message}")
            }
        }
    }

    fun stop() {
        markOffline()
    }

    private suspend fun isAuthenticated(): Boolean {
        return !sessionManager.accessToken.first().isNullOrBlank()
    }
}
