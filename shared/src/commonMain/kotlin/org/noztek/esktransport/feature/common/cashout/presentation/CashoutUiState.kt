package org.noztek.esktransport.feature.common.cashout.presentation

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletCashout

data class CashoutUiState(
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val isCancelling: Boolean = false,
    val walletBalance: Double = 0.0,
    val minimumWalletBalance: Double = 0.0,
    val currency: String = "PHP",
    val amountText: String = "100",
    val selectedCashoutPercent: Double? = null,
    val activeCashout: DriverWalletCashout? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val availableCashout: Double
        get() = (walletBalance - minimumWalletBalance).coerceAtLeast(0.0)

    val canGenerate: Boolean
        get() = !isGenerating &&
            !isCancelling &&
            !isLoading &&
            amountText.toDoubleOrNull()?.let { amount ->
                amount > 0.0
            } == true

    val canCancel: Boolean
        get() = !isGenerating && !isCancelling && activeCashout?.status == "pending"
}

const val CashoutMinimumAmount = 50.0
