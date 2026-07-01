package org.noztek.esktransport.feature.driver.wallet.domain.usecase

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class CreateDriverTopupUseCase(
    private val repository: DriverWalletRepository,
) {
    suspend operator fun invoke(
        amount: Double,
        currency: String = "PHP",
    ): Result<DriverWalletTopup> = repository.createTopup(amount = amount, currency = currency)
}
