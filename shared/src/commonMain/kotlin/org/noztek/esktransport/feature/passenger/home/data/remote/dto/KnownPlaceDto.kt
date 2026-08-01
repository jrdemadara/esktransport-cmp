package org.noztek.esktransport.feature.passenger.home.data.remote.dto

import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace

@Serializable
data class KnownPlacesResponseDto(
    val data: List<KnownPlaceDto> = emptyList(),
)

@Serializable
data class KnownPlaceDto(
    val id: Long,
    val name: String,
    val category: String = "place",
    val city: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

fun KnownPlaceDto.toDomain(): KnownPlace {
    return KnownPlace(
        id = id,
        name = name,
        category = category,
        city = city,
        address = address,
        latitude = latitude,
        longitude = longitude,
    )
}
