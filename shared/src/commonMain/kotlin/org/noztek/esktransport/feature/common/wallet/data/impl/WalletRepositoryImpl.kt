package org.noztek.esktransport.feature.common.wallet.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.wallet.data.remote.WalletApi
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletDashboard
import org.noztek.esktransport.feature.common.wallet.domain.repository.WalletRepository

class WalletRepositoryImpl(
    private val api: WalletApi,
) : WalletRepository {
    override suspend fun getWallet(): Result<WalletDashboard> {
        return try {
            Result.success(api.getWallet().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load wallet.")
            Result.failure(IllegalStateException(message))
        }
    }
}
