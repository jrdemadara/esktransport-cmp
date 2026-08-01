package org.noztek.esktransport.feature.passenger.settings.domain.model

data class SavedPlace(
    val id: Long,
    val placeType: SavedPlaceType,
    val label: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val sortOrder: Int,
    val isDefault: Boolean,
)

enum class SavedPlaceType {
    Home,
    Work,
    Custom,
}

data class SavedPlacePayload(
    val placeType: SavedPlaceType,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
)
