package org.noztek.esktransport.core.session.domain

import kotlinx.coroutines.flow.Flow

data class SessionUser(
    val userId: Long?,
    val name: String?,
    val phone: String?,
    val roles: Set<String>,
    val primaryRole: String?,
)

interface AuthSessionRepository {
    val user: Flow<SessionUser>
    val accessToken: Flow<String?>
    val isLoggedIn: Flow<Boolean>

    fun saveSession(
        userId: Long?,
        token: String,
        roles: List<String>,
        name: String?,
        phone: String?,
        expiresAtMs: Long?,
    )

    fun clearSession()

    fun cachedToken(): String?

    fun hasSeenStarter(): Boolean

    fun markStarterSeen()
}
