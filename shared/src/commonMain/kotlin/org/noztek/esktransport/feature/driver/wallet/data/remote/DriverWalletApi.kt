package org.noztek.esktransport.feature.driver.wallet.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.CreateDriverCashoutRequestDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.CreateDriverTopupRequestDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletCashoutResponseDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletResponseDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletTopupResponseDto

class DriverWalletApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getWallet(): DriverWalletResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/wallet").body()
    }

    suspend fun createTopup(request: CreateDriverTopupRequestDto): DriverWalletTopupResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/wallet/topups") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun cancelTopup(referenceCode: String): DriverWalletTopupResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/wallet/topups/$referenceCode/cancel") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun createCashout(request: CreateDriverCashoutRequestDto): DriverWalletCashoutResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/wallet/cashouts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun cancelCashout(referenceCode: String): DriverWalletCashoutResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/wallet/cashouts/$referenceCode/cancel") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
