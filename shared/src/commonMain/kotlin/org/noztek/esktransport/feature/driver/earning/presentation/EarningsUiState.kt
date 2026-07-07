package org.noztek.esktransport.feature.driver.earning.presentation

import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard

data class EarningsUiState(
    val isLoading: Boolean = false,
    val dashboard: RiderEarningsDashboard? = null,
    val error: String? = null,
)
