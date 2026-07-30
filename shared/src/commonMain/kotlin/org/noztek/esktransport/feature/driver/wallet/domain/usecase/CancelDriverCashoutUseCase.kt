package org.noztek.esktransport.feature.driver.wallet.domain.usecase

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletCashout
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class CancelDriverCashoutUseCase(
    private val repository: DriverWalletRepository,
) {
    suspend operator fun invoke(referenceCode: String): Result<DriverWalletCashout> {
        return repository.cancelCashout(referenceCode = referenceCode)
    }
}
