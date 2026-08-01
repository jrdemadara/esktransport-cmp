package org.noztek.esktransport.feature.passenger.wallet.presentation

import org.noztek.esktransport.feature.common.wallet.domain.model.WalletCashout
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletLedgerEntry
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletTopup

data class WalletUiState(
    val isLoading: Boolean = true,
    val balance: Double = 0.0,
    val currency: String = "PHP",
    val pendingTopups: List<WalletTopup> = emptyList(),
    val pendingCashouts: List<WalletCashout> = emptyList(),
    val recentLedgerEntries: List<WalletLedgerEntry> = emptyList(),
    val errorMessage: String? = null,
)
