package org.noztek.esktransport.feature.common.chat.domain.usecase

import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole
import org.noztek.esktransport.feature.common.chat.domain.repository.TripChatRepository

class GetTripChatMessagesUseCase(
    private val repository: TripChatRepository,
) {
    suspend operator fun invoke(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ): Result<List<TripChatMessage>> = repository.getMessages(bookingPublicId, role)
}
