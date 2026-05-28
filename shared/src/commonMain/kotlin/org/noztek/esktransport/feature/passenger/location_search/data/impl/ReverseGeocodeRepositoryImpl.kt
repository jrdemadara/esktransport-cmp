package org.noztek.esktransport.feature.passenger.location_search.data.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.ReverseGeocodeRepository

class ReverseGeocodeRepositoryImpl(
    private val client: HttpClient,
    private val mapboxConfig: MapboxConfig,
) : ReverseGeocodeRepository {
    override suspend fun resolveLabel(point: GeoPoint): String? {
        if (!mapboxConfig.hasAccessToken) return null
        return runCatching {
            val response = client.get("https://api.mapbox.com/geocoding/v5/mapbox.places/${point.longitude},${point.latitude}.json") {
                parameter("limit", 1)
                parameter("language", "en")
                parameter("types", "address,poi,place,locality,neighborhood")
                parameter("access_token", mapboxConfig.accessToken)
            }.body<MapboxGeocodeResponse>()
            response.features.firstOrNull()?.placeName?.let(::trimProvinceAndCountry)
        }.getOrNull()
    }

    @Serializable
    private data class MapboxGeocodeResponse(
        val features: List<MapboxFeature> = emptyList(),
    )

    @Serializable
    private data class MapboxFeature(
        @SerialName("place_name")
        val placeName: String? = null,
    )

    private fun trimProvinceAndCountry(label: String): String {
        val parts = label.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (parts.size <= 2) return label
        return parts.dropLast(2).joinToString(", ").ifBlank { label }
    }
}
