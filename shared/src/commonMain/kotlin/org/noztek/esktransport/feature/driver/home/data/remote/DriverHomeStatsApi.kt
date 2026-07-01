package org.noztek.esktransport.feature.driver.home.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.driver.home.data.remote.dto.DriverHomeStatsResponseDto

class DriverHomeStatsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getStats(): DriverHomeStatsResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/stats").body()
    }
}
