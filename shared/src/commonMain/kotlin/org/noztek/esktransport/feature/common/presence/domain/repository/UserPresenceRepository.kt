package org.noztek.esktransport.feature.common.presence.domain.repository

import org.noztek.esktransport.feature.common.presence.domain.model.UserPresence
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole

interface UserPresenceRepository {
    suspend fun getPresence(): Result<UserPresence>
    suspend fun heartbeat(role: UserPresenceRole?, context: UserPresenceContext?, metadata: Map<String, String> = emptyMap()): Result<UserPresence>
    suspend fun foreground(role: UserPresenceRole?, context: UserPresenceContext?, metadata: Map<String, String> = emptyMap()): Result<UserPresence>
    suspend fun background(role: UserPresenceRole?, context: UserPresenceContext?, metadata: Map<String, String> = emptyMap()): Result<UserPresence>
    suspend fun offline(role: UserPresenceRole?, context: UserPresenceContext?, metadata: Map<String, String> = emptyMap()): Result<UserPresence>
}
