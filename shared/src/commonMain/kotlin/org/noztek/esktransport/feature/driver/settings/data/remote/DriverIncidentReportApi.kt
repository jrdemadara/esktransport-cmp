package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverIncidentReportRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverIncidentReportsResponseDto

class DriverIncidentReportApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val endpoint = "${baseUrl.trimEnd('/')}/api/v1/rider/incident-reports"

    suspend fun getReports(): DriverIncidentReportsResponseDto {
        return client.get(endpoint).body()
    }

    suspend fun submitReport(request: DriverIncidentReportRequestDto): DriverIncidentReportsResponseDto {
        return client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
