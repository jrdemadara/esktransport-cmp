package org.noztek.esktransport.feature.passenger.location_search.presentation

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

sealed interface LocationSearchUiEvent {
    data class MoveCamera(
        val point: GeoPoint,
        val zoom: Double,
        val animated: Boolean = false,
    ) : LocationSearchUiEvent
}
