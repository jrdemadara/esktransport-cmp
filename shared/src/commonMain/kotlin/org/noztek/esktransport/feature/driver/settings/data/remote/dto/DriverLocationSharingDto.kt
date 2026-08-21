package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettingsPayload

@Serializable
data class DriverLocationSharingResponseDto(
    val data: DriverLocationSharingDto,
)

@Serializable
data class DriverLocationSharingDto(
    @SerialName("online_location_required")
    val onlineLocationRequired: Boolean = true,
    @SerialName("active_trip_location_required")
    val activeTripLocationRequired: Boolean = true,
    @SerialName("support_location_enabled")
    val supportLocationEnabled: Boolean = true,
    @SerialName("incident_location_enabled")
    val incidentLocationEnabled: Boolean = true,
)

@Serializable
data class DriverLocationSharingRequestDto(
    @SerialName("support_location_enabled")
    val supportLocationEnabled: Boolean,
    @SerialName("incident_location_enabled")
    val incidentLocationEnabled: Boolean,
)

fun DriverLocationSharingDto.toDomain(): DriverLocationSharingSettings {
    return DriverLocationSharingSettings(
        onlineLocationRequired = onlineLocationRequired,
        activeTripLocationRequired = activeTripLocationRequired,
        supportLocationEnabled = supportLocationEnabled,
        incidentLocationEnabled = incidentLocationEnabled,
    )
}

fun DriverLocationSharingSettingsPayload.toRequestDto(): DriverLocationSharingRequestDto {
    return DriverLocationSharingRequestDto(
        supportLocationEnabled = supportLocationEnabled,
        incidentLocationEnabled = incidentLocationEnabled,
    )
}
