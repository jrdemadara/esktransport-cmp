package org.noztek.esktransport.feature.driver.earning.domain.usecase

import org.noztek.esktransport.feature.driver.earning.domain.repository.RiderEarningsRepository

class GetRiderEarningsUseCase(
    private val repository: RiderEarningsRepository,
) {
    suspend operator fun invoke() = repository.getEarnings()
}
