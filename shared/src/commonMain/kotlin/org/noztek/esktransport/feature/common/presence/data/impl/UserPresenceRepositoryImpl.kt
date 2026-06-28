package org.noztek.esktransport.feature.common.presence.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.presence.data.remote.UserPresenceApi
import org.noztek.esktransport.feature.common.presence.data.remote.dto.UserPresenceDataDto
import org.noztek.esktransport.feature.common.presence.data.remote.dto.UserPresenceRequestDto
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresence
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceStatus
import org.noztek.esktransport.feature.common.presence.domain.repository.UserPresenceRepository

class UserPresenceRepositoryImpl(
    private val api: UserPresenceApi,
) : UserPresenceRepository {
    override suspend fun getPresence(): Result<UserPresence> {
        return runPresenceCall("Unable to get user presence.") {
            api.getPresence().data.toDomain()
        }
    }

    override suspend fun heartbeat(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String>,
    ): Result<UserPresence> {
        return runPresenceCall("Unable to send user heartbeat.") {
            api.heartbeat(toRequest(role, context, metadata)).data.toDomain()
        }
    }

    override suspend fun foreground(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String>,
    ): Result<UserPresence> {
        return runPresenceCall("Unable to update user presence.") {
            api.foreground(toRequest(role, context, metadata)).data.toDomain()
        }
    }

    override suspend fun background(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String>,
    ): Result<UserPresence> {
        return runPresenceCall("Unable to update user presence.") {
            api.background(toRequest(role, context, metadata)).data.toDomain()
        }
    }

    override suspend fun offline(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String>,
    ): Result<UserPresence> {
        return runPresenceCall("Unable to update user presence.") {
            api.offline(toRequest(role, context, metadata)).data.toDomain()
        }
    }

    private suspend fun runPresenceCall(
        fallbackMessage: String,
        block: suspend () -> UserPresence,
    ): Result<UserPresence> {
        return try {
            Result.success(block())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, fallbackMessage)
            Result.failure(IllegalStateException(message))
        }
    }

    private fun toRequest(
        role: UserPresenceRole?,
        context: UserPresenceContext?,
        metadata: Map<String, String>,
    ): UserPresenceRequestDto {
        return UserPresenceRequestDto(
            currentRole = role?.value,
            currentContext = context?.value,
            metadata = metadata.ifEmpty { null },
        )
    }

    private fun UserPresenceDataDto.toDomain(): UserPresence {
        return UserPresence(
            userId = userId,
            status = status.toPresenceStatus(),
            currentRole = currentRole.toPresenceRole(),
            currentContext = currentContext.toPresenceContext(),
            metadata = metadata.orEmpty(),
            lastSeenAt = lastSeenAt,
            lastForegroundedAt = lastForegroundedAt,
            lastBackgroundedAt = lastBackgroundedAt,
            lastOfflineAt = lastOfflineAt,
        )
    }

    private fun String.toPresenceStatus(): UserPresenceStatus {
        return when (lowercase()) {
            "online" -> UserPresenceStatus.Online
            "backgrounded" -> UserPresenceStatus.Backgrounded
            "offline" -> UserPresenceStatus.Offline
            else -> UserPresenceStatus.Unknown
        }
    }

    private fun String?.toPresenceRole(): UserPresenceRole? {
        return UserPresenceRole.entries.firstOrNull { it.value == this }
    }

    private fun String?.toPresenceContext(): UserPresenceContext? {
        return UserPresenceContext.entries.firstOrNull { it.value == this }
    }
}
