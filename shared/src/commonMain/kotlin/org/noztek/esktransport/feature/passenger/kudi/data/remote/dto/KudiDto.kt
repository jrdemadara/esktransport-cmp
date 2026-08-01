package org.noztek.esktransport.feature.passenger.kudi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class KudiSessionResponseDto(
    val data: KudiSessionPayloadDto,
)

@Serializable
data class KudiMessageResponseDto(
    val message: String? = null,
    val data: KudiMessagePayloadDto,
)

@Serializable
data class KudiSessionPayloadDto(
    val session: KudiSessionDto? = null,
    val messages: List<KudiMessageDto> = emptyList(),
    val state: JsonElement? = null,
)

@Serializable
data class KudiMessagePayloadDto(
    val session: KudiSessionDto? = null,
    val messages: List<KudiMessageDto> = emptyList(),
    val state: JsonElement? = null,
    val assistant: KudiAssistantDto? = null,
)

@Serializable
data class KudiSessionDto(
    @SerialName("public_id")
    val publicId: String,
    val status: String,
    val intent: String? = null,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("last_activity_at")
    val lastActivityAt: String? = null,
)

@Serializable
data class KudiMessageDto(
    val id: Long? = null,
    val sender: String,
    val message: String,
    val meta: JsonElement? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class KudiStateDto(
    val intent: String? = null,
    val allowed: Boolean? = null,
    @SerialName("missing_fields")
    val missingFields: List<String> = emptyList(),
    @SerialName("trip_details")
    val tripDetails: JsonElement? = null,
    val actions: List<JsonElement> = emptyList(),
    @SerialName("last_user_message")
    val lastUserMessage: String? = null,
    @SerialName("last_assistant_message")
    val lastAssistantMessage: String? = null,
)

@Serializable
data class KudiAssistantDto(
    val allowed: Boolean = true,
    val intent: String? = null,
    val reply: String,
    @SerialName("missing_fields")
    val missingFields: List<String> = emptyList(),
    @SerialName("trip_details")
    val tripDetails: JsonElement? = null,
    val actions: List<JsonElement> = emptyList(),
)

@Serializable
data class SendKudiMessageRequestDto(
    val message: String,
)
