package org.noztek.esktransport.feature.driver.wallet.domain.repository

import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletCashout
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup

interface DriverWalletRepository {
    suspend fun getWallet(): Result<DriverWalletDashboard>

    suspend fun createTopup(
        amount: Double,
        currency: String = "PHP",
    ): Result<DriverWalletTopup>

    suspend fun createCashout(
        amount: Double,
        currency: String = "PHP",
    ): Result<DriverWalletCashout>

    suspend fun cancelCashout(referenceCode: String): Result<DriverWalletCashout>
}
