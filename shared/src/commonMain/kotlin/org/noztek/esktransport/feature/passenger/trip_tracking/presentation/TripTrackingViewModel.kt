package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.core.realtime.model.PassengerTripLocationUpdatedEvent
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.LatestLocation
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripPoint
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
    private val realtimeCoordinator: PassengerRealtimeCoordinator,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripTrackingUIState())
    val uiState: StateFlow<TripTrackingUIState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<TripTrackingUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<TripTrackingUiEvent> = _uiEvents.asSharedFlow()
    private var realtimeJob: Job? = null

    fun loadTripData(bookingId: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { tripTrackingUseCase(bookingId) }
                .onSuccess { session ->
                    val stage = stageFor(status = session.status, phase = null)
                    val routes = buildRoutes(session = session, stage = stage)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripSession = session,
                        stage = stage,
                        riderToPickupRoute = routes.riderToPickupRoute,
                        driverToDestinationRoute = routes.driverToDestinationRoute,
                        pickupToDestinationRoute = routes.pickupToDestinationRoute,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unable to load trip details.",
                    )
                }
        }
    }

    fun startRealtime(bookingId: String) {
        if (bookingId.isBlank() || realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            realtimeCoordinator.subscribePassengerDriverAssigned()
            realtimeCoordinator.passengerTripLocationUpdated().collectLatest { event ->
                if (event.bookingPublicId != bookingId) return@collectLatest
                applyLocationUpdate(event)
            }
        }
    }

    fun stopRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
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

    private suspend fun applyLocationUpdate(event: PassengerTripLocationUpdatedEvent) {
        val currentSession = _uiState.value.tripSession ?: return
        val updatedSession = currentSession.copy(
            latestLocation = LatestLocation(
                latitude = event.latitude,
                longitude = event.longitude,
                bearing = event.bearing,
                speedKph = event.speedKph,
            ),
        )
        val stage = stageFor(status = updatedSession.status, phase = event.phase)
        val routes = withContext(ioDispatcher) {
            buildRoutes(session = updatedSession, stage = stage)
        }

        _uiState.value = _uiState.value.copy(
            tripSession = updatedSession,
            stage = stage,
            riderToPickupRoute = routes.riderToPickupRoute,
            driverToDestinationRoute = routes.driverToDestinationRoute,
            pickupToDestinationRoute = routes.pickupToDestinationRoute,
        )
    }

    private suspend fun buildRoutes(session: TripTrackingSession, stage: TripTrackingStage): StageRoutes {
        val pickupPoint = session.pickupPoint.toMapPoint()
        val destinationPoint = session.destinationPoint.toMapPoint()
        val riderToPickup = session.latestLocation?.let { latestLocation ->
            if (pickupPoint == null) return@let emptyList()
            mapboxDirectionsClient.getRoutePoints(
                originLongitude = latestLocation.longitude,
                originLatitude = latestLocation.latitude,
                destinationLongitude = pickupPoint.longitude,
                destinationLatitude = pickupPoint.latitude,
            ).getOrElse {
                listOf(
                    MapPoint(latestLocation.latitude, latestLocation.longitude),
                    pickupPoint,
                )
            }
        }.orEmpty()

        val driverToDestination = if (stage == TripTrackingStage.ToDropoff && session.latestLocation != null && destinationPoint != null) {
            val latestLocation = session.latestLocation
            mapboxDirectionsClient.getRoutePoints(
                originLongitude = latestLocation.longitude,
                originLatitude = latestLocation.latitude,
                destinationLongitude = destinationPoint.longitude,
                destinationLatitude = destinationPoint.latitude,
            ).getOrElse {
                listOf(
                    MapPoint(latestLocation.latitude, latestLocation.longitude),
                    destinationPoint,
                )
            }
        } else {
            emptyList()
        }

        val pickupToDestination = if (pickupPoint != null && destinationPoint != null) {
            mapboxDirectionsClient.getRoutePoints(
                originLongitude = pickupPoint.longitude,
                originLatitude = pickupPoint.latitude,
                destinationLongitude = destinationPoint.longitude,
                destinationLatitude = destinationPoint.latitude,
            ).getOrElse {
                listOf(pickupPoint, destinationPoint)
            }
        } else {
            emptyList()
        }

        return StageRoutes(
            riderToPickupRoute = riderToPickup,
            driverToDestinationRoute = driverToDestination,
            pickupToDestinationRoute = pickupToDestination,
        )
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }
}

private data class StageRoutes(
    val riderToPickupRoute: List<MapPoint>,
    val driverToDestinationRoute: List<MapPoint>,
    val pickupToDestinationRoute: List<MapPoint>,
)

private fun stageFor(status: String, phase: String?): TripTrackingStage {
    return when {
        phase == "to_destination" -> TripTrackingStage.ToDropoff
        status == "in_progress" -> TripTrackingStage.ToDropoff
        status == "arrived" || status == "arrived_pickup" || status == "waiting_pickup" -> TripTrackingStage.ArrivedPickup
        status == "completed" -> TripTrackingStage.Completed
        else -> TripTrackingStage.ToPickup
    }
}

private fun TripPoint.toMapPoint(): MapPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return MapPoint(lat, lng)
}
