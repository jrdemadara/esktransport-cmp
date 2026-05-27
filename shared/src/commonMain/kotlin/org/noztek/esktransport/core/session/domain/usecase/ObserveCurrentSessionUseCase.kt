package org.noztek.esktransport.core.session.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.noztek.esktransport.core.session.domain.AuthSessionRepository
import org.noztek.esktransport.core.session.domain.SessionUser

class ObserveCurrentSessionUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke(): Flow<SessionUser> = repository.user
}
