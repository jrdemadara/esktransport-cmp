package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.core.realtime.driver.DriverOnboardingRealtime

class UnsubscribeDriverOnboardingRealtimeUseCase(
    private val realtime: DriverOnboardingRealtime,
) {
    operator fun invoke() {
        realtime.unsubscribeDriverOnboarding()
    }
}
