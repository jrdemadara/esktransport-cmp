package org.noztek.esktransport.feature.driver.settings.domain.model

data class DriverLocationSharingSettings(
    val onlineLocationRequired: Boolean,
    val activeTripLocationRequired: Boolean,
    val supportLocationEnabled: Boolean,
    val incidentLocationEnabled: Boolean,
)

data class DriverLocationSharingSettingsPayload(
    val supportLocationEnabled: Boolean,
    val incidentLocationEnabled: Boolean,
)
