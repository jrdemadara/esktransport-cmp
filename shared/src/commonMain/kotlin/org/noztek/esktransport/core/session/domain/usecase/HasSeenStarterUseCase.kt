package org.noztek.esktransport.core.session.domain.usecase

import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class HasSeenStarterUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke(): Boolean = repository.hasSeenStarter()
}
