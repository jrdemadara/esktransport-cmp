package org.noztek.esktransport.feature.common.presence.domain.usecase

import org.noztek.esktransport.feature.common.presence.domain.model.UserPresence
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.common.presence.domain.repository.UserPresenceRepository

class MarkUserBackgroundUseCase(
    private val repository: UserPresenceRepository,
) {
    suspend operator fun invoke(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String> = emptyMap(),
    ): Result<UserPresence> = repository.background(role, context, metadata)
}
