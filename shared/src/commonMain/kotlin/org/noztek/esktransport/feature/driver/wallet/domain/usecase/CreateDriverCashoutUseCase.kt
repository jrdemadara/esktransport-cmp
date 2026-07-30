package org.noztek.esktransport.feature.driver.wallet.domain.usecase

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletCashout
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class CreateDriverCashoutUseCase(
    private val repository: DriverWalletRepository,
) {
    suspend operator fun invoke(
        amount: Double,
        currency: String = "PHP",
    ): Result<DriverWalletCashout> = repository.createCashout(amount = amount, currency = currency)
}
