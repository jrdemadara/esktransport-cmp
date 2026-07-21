package org.noztek.esktransport.feature.common.chat.presentation

import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole

data class TripChatUiState(
    val bookingPublicId: String = "",
    val role: TripChatParticipantRole = TripChatParticipantRole.Passenger,
    val messages: List<TripChatMessage> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)
