package org.noztek.esktransport.feature.common.wallet.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletResponseDto

class WalletApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getWallet(): WalletResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/wallet").body()
    }
}
