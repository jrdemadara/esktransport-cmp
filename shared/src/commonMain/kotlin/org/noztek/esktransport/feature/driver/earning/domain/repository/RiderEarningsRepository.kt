package org.noztek.esktransport.feature.driver.earning.domain.repository

import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard

interface RiderEarningsRepository {
    suspend fun getEarnings(): Result<RiderEarningsDashboard>
}
