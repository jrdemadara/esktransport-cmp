package org.noztek.esktransport.feature.common.chat.data.impl

import org.noztek.esktransport.feature.common.chat.data.remote.dto.TripChatMessageDto
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.toTripChatParticipantRole

fun TripChatMessageDto.toDomain(): TripChatMessage {
    return TripChatMessage(
        id = id,
        bookingPublicId = bookingPublicId,
        senderUserId = senderUserId,
        senderRole = senderRole.toTripChatParticipantRole(),
        senderName = senderName,
        message = message,
        sentAt = sentAt,
        isMine = isMine,
    )
}
