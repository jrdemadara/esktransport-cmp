package org.noztek.esktransport.feature.common.chat.domain.usecase

import org.noztek.esktransport.feature.common.chat.data.realtime.TripChatRealtime
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole

class SubscribeTripChatUseCase(
    private val realtime: TripChatRealtime,
) {
    operator fun invoke(role: TripChatParticipantRole) {
        realtime.subscribe(role)
    }
}
