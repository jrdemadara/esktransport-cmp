package org.noztek.esktransport.feature.common.presence.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPresenceRequestDto(
    @SerialName("current_role")
    val currentRole: String? = null,
    @SerialName("current_context")
    val currentContext: String? = null,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class UserPresenceResponseDto(
    val message: String? = null,
    val data: UserPresenceDataDto,
)

@Serializable
data class UserPresenceDataDto(
    @SerialName("user_id")
    val userId: Long,
    val status: String,
    @SerialName("current_role")
    val currentRole: String? = null,
    @SerialName("current_context")
    val currentContext: String? = null,
    val metadata: Map<String, String>? = null,
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null,
    @SerialName("last_foregrounded_at")
    val lastForegroundedAt: String? = null,
    @SerialName("last_backgrounded_at")
    val lastBackgroundedAt: String? = null,
    @SerialName("last_offline_at")
    val lastOfflineAt: String? = null,
)
