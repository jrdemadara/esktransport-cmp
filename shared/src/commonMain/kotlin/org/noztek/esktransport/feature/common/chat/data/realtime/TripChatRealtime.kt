package org.noztek.esktransport.feature.common.chat.data.realtime

import kotlinx.coroutines.flow.SharedFlow
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole

interface TripChatRealtime {
    fun subscribe(role: TripChatParticipantRole)
    fun unsubscribe()
    fun messages(): SharedFlow<TripChatMessage>
}
