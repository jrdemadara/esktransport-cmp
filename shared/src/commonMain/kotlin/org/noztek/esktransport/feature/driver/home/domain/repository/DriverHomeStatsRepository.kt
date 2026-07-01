package org.noztek.esktransport.feature.driver.home.domain.repository

import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats

interface DriverHomeStatsRepository {
    suspend fun getStats(): Result<DriverHomeStats>
}
