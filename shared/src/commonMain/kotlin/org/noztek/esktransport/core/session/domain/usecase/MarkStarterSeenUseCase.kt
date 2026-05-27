package org.noztek.esktransport.core.session.domain.usecase

import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class MarkStarterSeenUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke() {
        repository.markStarterSeen()
    }
}
