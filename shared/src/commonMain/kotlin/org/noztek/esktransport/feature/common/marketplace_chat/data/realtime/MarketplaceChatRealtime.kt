package org.noztek.esktransport.feature.common.marketplace_chat.data.realtime

import kotlinx.coroutines.flow.SharedFlow
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage

interface MarketplaceChatRealtime {
    fun subscribePassenger()
    fun subscribeOwner()
    fun unsubscribe()
    fun messages(): SharedFlow<MarketplaceChatMessage>
}
