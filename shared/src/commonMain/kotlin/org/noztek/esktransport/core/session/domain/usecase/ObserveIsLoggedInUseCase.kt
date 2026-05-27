package org.noztek.esktransport.core.session.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class ObserveIsLoggedInUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.isLoggedIn
}
