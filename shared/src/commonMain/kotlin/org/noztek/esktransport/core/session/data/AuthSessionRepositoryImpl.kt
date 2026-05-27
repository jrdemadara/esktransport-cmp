package org.noztek.esktransport.core.session.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.core.session.domain.AuthSessionRepository
import org.noztek.esktransport.core.session.domain.SessionUser

class AuthSessionRepositoryImpl(
    private val sessionManager: SessionManager,
) : AuthSessionRepository {

    override val user: Flow<SessionUser> = combine(
        sessionManager.userId,
        sessionManager.userName,
        sessionManager.userPhone,
        sessionManager.userRoles,
        sessionManager.userRole,
    ) { userId, name, phone, roles, primaryRole ->
        SessionUser(
            userId = userId,
            name = name,
            phone = phone,
            roles = roles,
            primaryRole = primaryRole,
        )
    }

    override val accessToken: Flow<String?> = sessionManager.accessToken

    override val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    override fun saveSession(
        userId: Long?,
        token: String,
        roles: List<String>,
        name: String?,
        phone: String?,
        expiresAtMs: Long?,
    ) {
        sessionManager.saveSession(
            userId = userId,
            token = token,
            roles = roles,
            name = name,
            phone = phone,
            expiresAtMs = expiresAtMs,
        )
    }

    override fun clearSession() {
        sessionManager.clear()
    }

    override fun cachedToken(): String? = sessionManager.cachedToken()

    override fun hasSeenStarter(): Boolean = sessionManager.hasSeenStarter()

    override fun markStarterSeen() {
        sessionManager.markStarterSeen()
    }
}
