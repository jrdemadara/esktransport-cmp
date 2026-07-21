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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.core.realtime.model.PassengerTripCompletedEvent
import org.noztek.esktransport.core.realtime.model.PassengerTripLocationUpdatedEvent
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetPassengerActiveBookingUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.LatestLocation
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripPoint
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.CancelPassengerTripUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.SubmitPassengerTripFeedbackUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.TripTrackingUseCase

sealed class TripTrackingUiEvent {
    data object NavigateToBookingReview : TripTrackingUiEvent()
    data object NavigateToHome : TripTrackingUiEvent()
}

class TripTrackingViewModel(
    private val tripTrackingUseCase: TripTrackingUseCase,
    private val cancelPassengerTripUseCase: CancelPassengerTripUseCase,
    private val submitPassengerTripFeedbackUseCase: SubmitPassengerTripFeedbackUseCase,
    private val getPassengerActiveBookingUseCase: GetPassengerActiveBookingUseCase,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val realtimeCoordinator: PassengerRealtimeCoordinator,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripTrackingUIState())
    val uiState: StateFlow<TripTrackingUIState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<TripTrackingUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<TripTrackingUiEvent> = _uiEvents.asSharedFlow()
    private var realtimeJob: Job? = null
    private var refreshJob: Job? = null

    fun loadTripData(bookingId: String) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { tripTrackingUseCase(bookingId) }
                .onSuccess { session ->
                    val sessionWithFare = session.withActiveBookingFareFallback(bookingId)
                    val stage = stageFor(status = session.status, phase = null)
                    val routes = buildRoutes(session = sessionWithFare, stage = stage)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tripSession = sessionWithFare,
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

    private suspend fun TripTrackingSession.withActiveBookingFareFallback(bookingId: String): TripTrackingSession {
        if (finalFare != null) return this

        val activeBooking = getPassengerActiveBookingUseCase().getOrNull()
            ?.takeIf { it.bookingPublicId == bookingId }
            ?: return this

        return copy(
            finalFare = activeBooking.finalFare,
            currency = activeBooking.currency ?: currency,
        )
    }

    fun startRealtime(bookingId: String) {
        if (bookingId.isBlank() || realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            realtimeCoordinator.subscribePassengerDriverAssigned()
            launch {
                realtimeCoordinator.passengerTripLocationUpdated().collectLatest { event ->
                    if (event.bookingPublicId != bookingId) return@collectLatest
                    applyLocationUpdate(bookingId, event)
                }
            }
            launch {
                realtimeCoordinator.passengerTripCompleted().collectLatest { event ->
                    if (event.bookingPublicId != bookingId) return@collectLatest
                    applyTripCompleted(event)
                }
            }
        }
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(TRIP_LOCATION_REFRESH_INTERVAL_MS)
                refreshTripLocation(bookingId)
            }
        }
    }

    fun stopRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
        refreshJob?.cancel()
        refreshJob = null
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

    private fun applyLocationUpdate(
        bookingId: String,
        event: PassengerTripLocationUpdatedEvent,
    ) {
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
        val shouldRebuildRoutes = _uiState.value.shouldRebuildRoutesFor(stage)
        _uiState.value = _uiState.value.copy(
            tripSession = updatedSession,
            stage = stage,
        )
        if (!shouldRebuildRoutes) return

        viewModelScope.launch(ioDispatcher) {
            val routes = buildRoutes(session = updatedSession, stage = stage)
            val latestState = _uiState.value
            if (latestState.tripSession?.bookingPublicId != bookingId) return@launch
            val latestLocation = latestState.tripSession.latestLocation
            if (latestLocation?.latitude != event.latitude || latestLocation.longitude != event.longitude) return@launch

            _uiState.value = latestState.copy(
                riderToPickupRoute = routes.riderToPickupRoute,
                driverToDestinationRoute = routes.driverToDestinationRoute,
                pickupToDestinationRoute = routes.pickupToDestinationRoute,
            )
        }
    }

    private fun applyTripCompleted(event: PassengerTripCompletedEvent) {
        val currentSession = _uiState.value.tripSession
        val updatedSession = currentSession?.copy(
            status = "completed",
            finalFare = event.finalFare ?: currentSession.finalFare,
            currency = event.currency ?: currentSession.currency,
        )
        _uiState.value = _uiState.value.copy(
            tripSession = updatedSession,
            stage = TripTrackingStage.Completed,
            showFeedback = true,
        )
    }

    fun submitFeedback(bookingPublicId: String, rating: Int, comment: String?) {
        if (bookingPublicId.isBlank() || _uiState.value.isSubmittingFeedback) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true, error = null)
            val result = withContext(ioDispatcher) {
                submitPassengerTripFeedbackUseCase(
                    bookingPublicId = bookingPublicId,
                    rating = rating,
                    comment = comment,
                )
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    showFeedback = false,
                )
                _uiEvents.tryEmit(TripTrackingUiEvent.NavigateToHome)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    error = error.message ?: "Failed to submit feedback.",
                )
            }
        }
    }

    fun skipFeedback() {
        _uiState.value = _uiState.value.copy(showFeedback = false)
        _uiEvents.tryEmit(TripTrackingUiEvent.NavigateToHome)
    }

    private suspend fun refreshTripLocation(bookingId: String) {
        if (bookingId.isBlank()) return
        runCatching {
            withContext(ioDispatcher) {
                tripTrackingUseCase(bookingId)
            }
        }.onSuccess { session ->
            val current = _uiState.value.tripSession
            val latestLocation = session.latestLocation ?: return@onSuccess
            val updatedSession = (current ?: session).copy(
                status = session.status,
                latestLocation = latestLocation,
                finalFare = current?.finalFare ?: session.finalFare,
                currency = current?.currency ?: session.currency,
            )
            val stage = stageFor(status = updatedSession.status, phase = null)
            val shouldRebuildRoutes = _uiState.value.shouldRebuildRoutesFor(stage)
            _uiState.value = _uiState.value.copy(
                tripSession = updatedSession,
                stage = stage,
            )
            if (!shouldRebuildRoutes) return@onSuccess

            viewModelScope.launch(ioDispatcher) {
                val routes = buildRoutes(session = updatedSession, stage = stage)
                val latestState = _uiState.value
                val stateLocation = latestState.tripSession?.latestLocation
                if (latestState.tripSession?.bookingPublicId != bookingId) return@launch
                if (stateLocation?.latitude != latestLocation.latitude || stateLocation.longitude != latestLocation.longitude) return@launch
                _uiState.value = latestState.copy(
                    riderToPickupRoute = routes.riderToPickupRoute,
                    driverToDestinationRoute = routes.driverToDestinationRoute,
                    pickupToDestinationRoute = routes.pickupToDestinationRoute,
                )
            }
        }
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

private const val TRIP_LOCATION_REFRESH_INTERVAL_MS = 5_000L

private fun TripTrackingUIState.shouldRebuildRoutesFor(stage: TripTrackingStage): Boolean {
    if (this.stage != stage) return true
    return when (stage) {
        TripTrackingStage.ToPickup -> riderToPickupRoute.isEmpty() || pickupToDestinationRoute.isEmpty()
        TripTrackingStage.ArrivedPickup -> pickupToDestinationRoute.isEmpty()
        TripTrackingStage.ToDropoff -> driverToDestinationRoute.isEmpty() || pickupToDestinationRoute.isEmpty()
        TripTrackingStage.Completed -> pickupToDestinationRoute.isEmpty()
    }
}

private fun stageFor(status: String, phase: String?): TripTrackingStage {
    return when {
        phase == "to_destination" -> TripTrackingStage.ToDropoff
        status == "in_progress" -> TripTrackingStage.ToDropoff
        status == "arriving_pickup" || status == "arrived" || status == "arrived_pickup" || status == "waiting_pickup" -> TripTrackingStage.ArrivedPickup
        status == "completed" -> TripTrackingStage.Completed
        else -> TripTrackingStage.ToPickup
    }
}

private fun TripPoint.toMapPoint(): MapPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return MapPoint(lat, lng)
}
