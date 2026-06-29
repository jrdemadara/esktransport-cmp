package org.noztek.esktransport.core.map

import androidx.compose.ui.graphics.Color

data class MapboxConfig(
    val accessToken: String,
    val defaultStyle: MapboxStyle = MapboxStyle.STANDARD,
) {
    val hasAccessToken: Boolean = accessToken.isNotBlank() && accessToken != PLACEHOLDER_TOKEN

    companion object {
        const val PLACEHOLDER_TOKEN = "YOUR_MAPBOX_PUBLIC_TOKEN"
    }
}

enum class MapboxStyle(
    val uri: String,
) {
    STANDARD("mapbox://styles/mapbox/standard"),
    STREETS("mapbox://styles/mapbox/streets-v12"),
    LIGHT("mapbox://styles/mapbox/light-v11"),
    DARK("mapbox://styles/mapbox/dark-v11"),
    SATELLITE_STREETS("mapbox://styles/mapbox/satellite-streets-v12"),
}

data class MapPoint(
    val latitude: Double,
    val longitude: Double,
)

data class MapCameraDefaults(
    val zoom: Double = 14.0,
    val pitch: Double = 0.0,
    val bearing: Double = 0.0,
)

data class MapMarker(
    val id: String,
    val point: MapPoint,
    val color: Color = Color(0xFF2563EB),
    val radius: Double = 7.0,
    val icon: MapMarkerIcon? = null,
)

enum class MapMarkerIcon(
    val assetFileName: String,
) {
    DriverLocation("driver_marker.png"),
    PickupPassenger("passenger_marker.png"),
    DestinationFlag("flag.png"),
}

data class MapRouteLine(
    val id: String,
    val points: List<MapPoint>,
    val color: Color = Color(0xFF2563EB),
    val width: Double = 5.0,
    val animatedAntPath: Boolean = false,
    val opacity: Double = 1.0,
    val dashPattern: List<Double> = emptyList(),
)
