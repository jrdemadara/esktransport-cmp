package org.noztek.esktransport.core.session.domain.usecase

import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class SaveSessionUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke(
        userId: Long?,
        token: String,
        roles: List<String>,
        name: String?,
        phone: String?,
        expiresAtMs: Long?,
    ) {
        repository.saveSession(
            userId = userId,
            token = token,
            roles = roles,
            name = name,
            phone = phone,
            expiresAtMs = expiresAtMs,
        )
    }
}
