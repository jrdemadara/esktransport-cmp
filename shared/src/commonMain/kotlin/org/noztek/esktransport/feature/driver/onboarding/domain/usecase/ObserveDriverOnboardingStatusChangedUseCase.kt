package org.noztek.esktransport.feature.driver.onboarding.domain.usecase

import org.noztek.esktransport.core.realtime.driver.DriverOnboardingRealtime

class ObserveDriverOnboardingStatusChangedUseCase(
    private val realtime: DriverOnboardingRealtime,
) {
    operator fun invoke() = realtime.onboardingStatusChanged()
}
