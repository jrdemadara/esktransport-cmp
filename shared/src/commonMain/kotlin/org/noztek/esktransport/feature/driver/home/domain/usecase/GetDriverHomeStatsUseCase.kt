package org.noztek.esktransport.feature.driver.home.domain.usecase

import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeStatsRepository

class GetDriverHomeStatsUseCase(
    private val repository: DriverHomeStatsRepository,
) {
    suspend operator fun invoke(): Result<DriverHomeStats> = repository.getStats()
}
