package org.noztek.esktransport.feature.passenger.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlaceType

@Serializable
data class SavedPlacesResponseDto(
    val data: List<SavedPlaceDto> = emptyList(),
)

@Serializable
data class SavedPlaceResponseDto(
    val message: String? = null,
    val data: SavedPlaceDto,
)

@Serializable
data class SavedPlaceMessageResponseDto(
    val message: String? = null,
)

@Serializable
data class SavedPlaceDto(
    val id: Long,
    @SerialName("place_type")
    val placeType: String,
    val label: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("is_default")
    val isDefault: Boolean = false,
)

@Serializable
data class SavedPlaceRequestDto(
    @SerialName("place_type")
    val placeType: String,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("is_default")
    val isDefault: Boolean = false,
)

fun SavedPlaceDto.toDomain(): SavedPlace {
    return SavedPlace(
        id = id,
        placeType = placeType.toSavedPlaceType(),
        label = label,
        address = address,
        latitude = latitude,
        longitude = longitude,
        sortOrder = sortOrder,
        isDefault = isDefault,
    )
}

fun SavedPlacePayload.toRequestDto(): SavedPlaceRequestDto {
    return SavedPlaceRequestDto(
        placeType = placeType.toApiValue(),
        label = label,
        address = address,
        latitude = latitude,
        longitude = longitude,
        sortOrder = sortOrder,
        isDefault = isDefault,
    )
}

private fun String.toSavedPlaceType(): SavedPlaceType {
    return when (lowercase()) {
        "home" -> SavedPlaceType.Home
        "work" -> SavedPlaceType.Work
        else -> SavedPlaceType.Custom
    }
}

private fun SavedPlaceType.toApiValue(): String {
    return when (this) {
        SavedPlaceType.Home -> "home"
        SavedPlaceType.Work -> "work"
        SavedPlaceType.Custom -> "custom"
    }
}
