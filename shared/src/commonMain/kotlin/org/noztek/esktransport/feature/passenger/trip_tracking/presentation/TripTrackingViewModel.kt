package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.CancelPassengerTripUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.TripTrackingUseCase

sealed class TripTrackingUiEvent {
    data object NavigateToBookingReview : TripTrackingUiEvent()
}

class TripTrackingViewModel(
    private val tripTrackingUseCase: TripTrackingUseCase,
    private val cancelPassengerTripUseCase: CancelPassengerTripUseCase,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripTrackingUIState())
    val uiState: StateFlow<TripTrackingUIState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<TripTrackingUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<TripTrackingUiEvent> = _uiEvents.asSharedFlow()

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

    fun cancelTrip(bookingPublicId: String) {
        if (bookingPublicId.isBlank() || _uiState.value.isCancelling) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, error = null)
            val result = withContext(ioDispatcher) {
                cancelPassengerTripUseCase(bookingPublicId)
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isCancelling = false)
                _uiEvents.tryEmit(TripTrackingUiEvent.NavigateToBookingReview)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCancelling = false,
                    error = error.message ?: "Cancel trip failed.",
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
