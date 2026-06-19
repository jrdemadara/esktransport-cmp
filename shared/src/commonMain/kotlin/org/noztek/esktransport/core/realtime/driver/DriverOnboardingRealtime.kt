package org.noztek.esktransport.core.realtime.driver

import kotlinx.coroutines.flow.SharedFlow
import org.noztek.esktransport.core.realtime.model.DriverOnboardingStatusChangedEvent

interface DriverOnboardingRealtime {
    fun subscribeDriverOnboarding(driverId: Long)
    fun unsubscribeDriverOnboarding()
    fun onboardingStatusChanged(): SharedFlow<DriverOnboardingStatusChangedEvent>
}
