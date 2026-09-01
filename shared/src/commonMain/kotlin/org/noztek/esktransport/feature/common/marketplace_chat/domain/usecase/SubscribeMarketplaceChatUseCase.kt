package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.data.realtime.MarketplaceChatRealtime
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatParticipantRole

class SubscribeMarketplaceChatUseCase(
    private val realtime: MarketplaceChatRealtime,
) {
    operator fun invoke(role: MarketplaceChatParticipantRole) {
        when (role) {
            MarketplaceChatParticipantRole.Passenger -> realtime.subscribePassenger()
            MarketplaceChatParticipantRole.Owner -> realtime.subscribeOwner()
        }
    }
}
