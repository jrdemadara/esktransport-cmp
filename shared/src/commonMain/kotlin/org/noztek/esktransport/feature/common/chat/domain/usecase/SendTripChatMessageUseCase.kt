package org.noztek.esktransport.feature.common.chat.domain.usecase

import org.noztek.esktransport.feature.common.chat.domain.model.SendTripChatMessagePayload
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.repository.TripChatRepository

class SendTripChatMessageUseCase(
    private val repository: TripChatRepository,
) {
    suspend operator fun invoke(payload: SendTripChatMessagePayload): Result<TripChatMessage> {
        return repository.sendMessage(payload)
    }
}
