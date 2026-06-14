package org.noztek.esktransport.core.realtime.passenger

import kotlinx.coroutines.flow.SharedFlow
import org.noztek.esktransport.core.realtime.model.PassengerBookingAcceptedEvent
import org.noztek.esktransport.core.realtime.model.PassengerBookingOfferExpiredEvent
import org.noztek.esktransport.core.realtime.model.PassengerBookingSearchExpiredEvent
import org.noztek.esktransport.core.realtime.model.PassengerDriverAssignedEvent

interface PassengerRealtimeCoordinator {
    fun subscribePassengerDriverAssigned()
    fun unsubscribePassengerDriverAssigned()
    fun passengerDriverAssigned(): SharedFlow<PassengerDriverAssignedEvent>
    fun passengerBookingAccepted(): SharedFlow<PassengerBookingAcceptedEvent>
    fun passengerBookingOfferExpired(): SharedFlow<PassengerBookingOfferExpiredEvent>
    fun passengerBookingSearchExpired(): SharedFlow<PassengerBookingSearchExpiredEvent>
}
