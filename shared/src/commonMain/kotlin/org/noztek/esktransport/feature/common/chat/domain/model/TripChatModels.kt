package org.noztek.esktransport.feature.common.chat.domain.model

enum class TripChatParticipantRole(val apiValue: String) {
    Passenger("passenger"),
    Driver("driver"),
}

data class TripChatMessage(
    val id: Long,
    val bookingPublicId: String,
    val senderUserId: Long,
    val senderRole: TripChatParticipantRole,
    val senderName: String,
    val message: String,
    val sentAt: String?,
    val isMine: Boolean,
)

data class SendTripChatMessagePayload(
    val bookingPublicId: String,
    val role: TripChatParticipantRole,
    val message: String,
)

fun String.toTripChatParticipantRole(): TripChatParticipantRole {
    return when (trim().lowercase()) {
        "driver", "rider" -> TripChatParticipantRole.Driver
        else -> TripChatParticipantRole.Passenger
    }
}
