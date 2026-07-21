package org.noztek.esktransport.feature.common.chat.domain.repository

import org.noztek.esktransport.feature.common.chat.domain.model.SendTripChatMessagePayload
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole

interface TripChatRepository {
    suspend fun getMessages(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ): Result<List<TripChatMessage>>

    suspend fun sendMessage(payload: SendTripChatMessagePayload): Result<TripChatMessage>
}
