package org.noztek.esktransport.core.map

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MapboxDirectionsClient(
    private val client: HttpClient,
    private val config: MapboxConfig,
) {
    suspend fun getRoutePoints(
        originLongitude: Double,
        originLatitude: Double,
        destinationLongitude: Double,
        destinationLatitude: Double,
    ): Result<List<MapPoint>> {
        if (!config.hasAccessToken) {
            return Result.success(
                listOf(
                    MapPoint(originLatitude, originLongitude),
                    MapPoint(destinationLatitude, destinationLongitude),
                ),
            )
        }

        return runCatching {
            val response = client.get(
                "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                    "$originLongitude,$originLatitude;$destinationLongitude,$destinationLatitude",
            ) {
                parameter("geometries", "geojson")
                parameter("overview", "full")
                parameter("access_token", config.accessToken)
            }.body<MapboxDirectionsResponse>()

            response.routes.firstOrNull()
                ?.geometry
                ?.coordinates
                ?.mapNotNull { coordinate ->
                    val longitude = coordinate.getOrNull(0) ?: return@mapNotNull null
                    val latitude = coordinate.getOrNull(1) ?: return@mapNotNull null
                    MapPoint(latitude = latitude, longitude = longitude)
                }
                ?.takeIf { it.size >= 2 }
                ?: listOf(
                    MapPoint(originLatitude, originLongitude),
                    MapPoint(destinationLatitude, destinationLongitude),
                )
        }
    }

    @Serializable
    private data class MapboxDirectionsResponse(
        val routes: List<MapboxRoute> = emptyList(),
    )

    @Serializable
    private data class MapboxRoute(
        val geometry: MapboxGeometry? = null,
    )

    @Serializable
    private data class MapboxGeometry(
        @SerialName("coordinates")
        val coordinates: List<List<Double>> = emptyList(),
    )
}
