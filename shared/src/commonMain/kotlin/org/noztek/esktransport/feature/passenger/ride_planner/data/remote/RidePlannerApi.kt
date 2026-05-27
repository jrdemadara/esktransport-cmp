package org.noztek.esktransport.feature.passenger.ride_planner.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import org.noztek.esktransport.feature.passenger.ride_planner.data.remote.dto.RidePlannerRequestDto
import org.noztek.esktransport.feature.passenger.ride_planner.data.remote.dto.RidePlannerResponseDto

class RidePlannerApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getAvailability(request: RidePlannerRequestDto): RidePlannerResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/planner/availability") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
