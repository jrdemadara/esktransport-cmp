package org.noztek.esktransport.feature.common.wallet.domain.repository

import org.noztek.esktransport.feature.common.wallet.domain.model.WalletDashboard

interface WalletRepository {
    suspend fun getWallet(): Result<WalletDashboard>
}
