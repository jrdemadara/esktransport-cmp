package org.noztek.esktransport.feature.common.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TripChatMessagesResponseDto(
    val data: List<TripChatMessageDto> = emptyList(),
)

@Serializable
data class TripChatMessageResponseDto(
    val message: String? = null,
    val data: TripChatMessageDto,
)

@Serializable
data class TripChatMessageDto(
    val id: Long,
    @SerialName("booking_public_id")
    val bookingPublicId: String,
    @SerialName("sender_user_id")
    val senderUserId: Long,
    @SerialName("sender_role")
    val senderRole: String,
    @SerialName("sender_name")
    val senderName: String,
    val message: String,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("is_mine")
    val isMine: Boolean = false,
)

@Serializable
data class SendTripChatMessageRequestDto(
    val message: String,
)
