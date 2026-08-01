package org.noztek.esktransport.feature.driver.wallet.presentation

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletLedgerEntry

data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val entries: List<DriverWalletLedgerEntry> = emptyList(),
    val errorMessage: String? = null,
)
