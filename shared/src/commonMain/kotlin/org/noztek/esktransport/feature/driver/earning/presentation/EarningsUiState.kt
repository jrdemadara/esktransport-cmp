package org.noztek.esktransport.feature.driver.earning.presentation

import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard

data class EarningsUiState(
    val isLoading: Boolean = false,
    val dashboard: RiderEarningsDashboard? = null,
    val walletDashboard: DriverWalletDashboard? = null,
    val error: String? = null,
)
