package org.noztek.esktransport.feature.common.chat.domain.usecase

import org.noztek.esktransport.feature.common.chat.data.realtime.TripChatRealtime

class UnsubscribeTripChatUseCase(
    private val realtime: TripChatRealtime,
) {
    operator fun invoke() {
        realtime.unsubscribe()
    }
}
