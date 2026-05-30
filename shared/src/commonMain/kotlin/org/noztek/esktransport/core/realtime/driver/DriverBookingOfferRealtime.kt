package org.noztek.esktransport.core.realtime.driver

import kotlinx.coroutines.flow.SharedFlow
import org.noztek.esktransport.core.realtime.model.DriverBookingOfferedEvent

interface DriverBookingOfferRealtime {
    fun subscribeDriverBookingOffers()
    fun unsubscribeDriverBookingOffers()
    fun driverBookingOffers(): SharedFlow<DriverBookingOfferedEvent>
}
