package org.noztek.esktransport.core.session.domain.usecase

import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class ClearSessionUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke() {
        repository.clearSession()
    }
}
