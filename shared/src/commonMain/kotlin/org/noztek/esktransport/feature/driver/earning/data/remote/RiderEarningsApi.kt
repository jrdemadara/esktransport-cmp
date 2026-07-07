package org.noztek.esktransport.feature.driver.earning.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.driver.earning.data.remote.dto.RiderEarningsResponseDto

class RiderEarningsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getEarnings(): RiderEarningsResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/earnings").body()
    }
}
