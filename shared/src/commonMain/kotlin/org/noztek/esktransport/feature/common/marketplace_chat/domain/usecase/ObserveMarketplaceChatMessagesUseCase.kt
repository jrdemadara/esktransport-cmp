package org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase

import org.noztek.esktransport.feature.common.marketplace_chat.data.realtime.MarketplaceChatRealtime

class ObserveMarketplaceChatMessagesUseCase(
    private val realtime: MarketplaceChatRealtime,
) {
    operator fun invoke() = realtime.messages()
}
