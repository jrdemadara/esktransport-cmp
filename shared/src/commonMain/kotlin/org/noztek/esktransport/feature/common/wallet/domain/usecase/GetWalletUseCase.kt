package org.noztek.esktransport.feature.common.wallet.domain.usecase

import org.noztek.esktransport.feature.common.wallet.domain.model.WalletDashboard
import org.noztek.esktransport.feature.common.wallet.domain.repository.WalletRepository

class GetWalletUseCase(
    private val repository: WalletRepository,
) {
    suspend operator fun invoke(): Result<WalletDashboard> = repository.getWallet()
}
