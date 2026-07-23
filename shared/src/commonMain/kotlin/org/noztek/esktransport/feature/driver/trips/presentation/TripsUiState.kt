package org.noztek.esktransport.feature.driver.trips.presentation

import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsDashboard

data class TripsUiState(
    val isLoading: Boolean = false,
    val dashboard: DriverTripsDashboard? = null,
    val errorMessage: String? = null,
)
