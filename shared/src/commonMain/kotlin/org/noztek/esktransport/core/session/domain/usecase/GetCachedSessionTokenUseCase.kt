package org.noztek.esktransport.core.session.domain.usecase

import org.noztek.esktransport.core.session.domain.AuthSessionRepository

class GetCachedSessionTokenUseCase(
    private val repository: AuthSessionRepository,
) {
    operator fun invoke(): String? = repository.cachedToken()
}
