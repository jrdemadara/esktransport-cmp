package org.noztek.esktransport.feature.driver.wallet.domain.usecase

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class GetDriverWalletUseCase(
    private val repository: DriverWalletRepository,
) {
    suspend operator fun invoke(): Result<DriverWalletDashboard> = repository.getWallet()
}
