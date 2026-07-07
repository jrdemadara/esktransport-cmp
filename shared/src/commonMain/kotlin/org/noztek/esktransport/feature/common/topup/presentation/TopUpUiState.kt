package org.noztek.esktransport.feature.common.topup.presentation

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup

data class TopUpUiState(
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val walletBalance: Double = 0.0,
    val currency: String = "PHP",
    val amountText: String = "500",
    val selectedPresetAmount: Double = 500.0,
    val activeTopup: DriverWalletTopup? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val canGenerate: Boolean
        get() = !isGenerating && amountText.toDoubleOrNull()?.let { it >= TopUpMinimumAmount } == true
}

const val TopUpMinimumAmount = 50.0
