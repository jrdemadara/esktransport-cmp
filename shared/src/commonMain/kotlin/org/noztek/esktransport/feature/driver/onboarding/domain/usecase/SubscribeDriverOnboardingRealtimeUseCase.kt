package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.core.realtime.driver.DriverOnboardingRealtime

class SubscribeDriverOnboardingRealtimeUseCase(
    private val realtime: DriverOnboardingRealtime,
) {
    operator fun invoke(driverId: Long) {
        realtime.subscribeDriverOnboarding(driverId)
    }
}
