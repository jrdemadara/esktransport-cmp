package org.noztek.esktransport.feature.driver.wallet.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.wallet.data.remote.DriverWalletApi
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.CreateDriverTopupRequestDto
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository

class DriverWalletRepositoryImpl(
    private val api: DriverWalletApi,
) : DriverWalletRepository {
    override suspend fun getWallet(): Result<DriverWalletDashboard> {
        return try {
            Result.success(api.getWallet().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load driver wallet.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun createTopup(
        amount: Double,
        currency: String,
    ): Result<DriverWalletTopup> {
        return try {
            val response = api.createTopup(
                CreateDriverTopupRequestDto(
                    amount = amount,
                    currency = currency,
                ),
            )
            Result.success(response.data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to create top-up reference.")
            Result.failure(IllegalStateException(message))
        }
    }
}
