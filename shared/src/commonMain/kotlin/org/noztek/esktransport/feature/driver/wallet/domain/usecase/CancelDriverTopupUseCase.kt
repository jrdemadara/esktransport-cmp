package org.noztek.esktransport.feature.driver.wallet.domain.usecase

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class CancelDriverTopupUseCase(
    private val repository: DriverWalletRepository,
) {
    suspend operator fun invoke(referenceCode: String): Result<DriverWalletTopup> {
        return repository.cancelTopup(referenceCode = referenceCode)
    }
}
