package org.noztek.esktransport.feature.common.chat.domain.usecase

import org.noztek.esktransport.feature.common.chat.data.realtime.TripChatRealtime

class ObserveTripChatMessagesUseCase(
    private val realtime: TripChatRealtime,
) {
    operator fun invoke() = realtime.messages()
}
