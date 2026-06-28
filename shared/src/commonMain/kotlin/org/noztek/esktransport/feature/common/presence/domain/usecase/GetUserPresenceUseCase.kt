package org.noztek.esktransport.feature.common.presence.domain.usecase

import org.noztek.esktransport.feature.common.presence.domain.model.UserPresence
import org.noztek.esktransport.feature.common.presence.domain.repository.UserPresenceRepository

class GetUserPresenceUseCase(
    private val repository: UserPresenceRepository,
) {
    suspend operator fun invoke(): Result<UserPresence> = repository.getPresence()
}
