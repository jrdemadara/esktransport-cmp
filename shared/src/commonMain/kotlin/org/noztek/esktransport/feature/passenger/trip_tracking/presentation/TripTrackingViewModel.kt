package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.TripTrackingUseCase

class TripTrackingViewModel(
    private val tripTrackingUseCase: TripTrackingUseCase,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripTrackingUIState())
    val uiState: StateFlow<TripTrackingUIState> = _uiState.asStateFlow()

    fun loadTripData(bookingId: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { tripTrackingUseCase(bookingId) }
                .onSuccess { session ->
                    _uiState.value = _uiState.value.copy(isLoading = false, tripSession = session)
                    fetchRoutes(session)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unable to load trip details.",
                    )
                }
        }
    }

    private suspend fun fetchRoutes(session: TripTrackingSession) {
        val riderToPickup = session.latestLocation?.let { latestLocation ->
            mapboxDirectionsClient.getRoutePoints(
                originLongitude = latestLocation.longitude,
                originLatitude = latestLocation.latitude,
                destinationLongitude = session.pickupPoint.longitude,
                destinationLatitude = session.pickupPoint.latitude,
            ).getOrElse {
                listOf(
                    MapPoint(latestLocation.latitude, latestLocation.longitude),
                    MapPoint(session.pickupPoint.latitude, session.pickupPoint.longitude),
                )
            }
        }.orEmpty()

        val pickupToDestination = mapboxDirectionsClient.getRoutePoints(
            originLongitude = session.pickupPoint.longitude,
            originLatitude = session.pickupPoint.latitude,
            destinationLongitude = session.destinationPoint.longitude,
            destinationLatitude = session.destinationPoint.latitude,
        ).getOrElse {
            listOf(
                MapPoint(session.pickupPoint.latitude, session.pickupPoint.longitude),
                MapPoint(session.destinationPoint.latitude, session.destinationPoint.longitude),
            )
        }

        _uiState.value = _uiState.value.copy(
            riderToPickupRoute = riderToPickup,
            pickupToDestinationRoute = pickupToDestination,
        )
    }
}
