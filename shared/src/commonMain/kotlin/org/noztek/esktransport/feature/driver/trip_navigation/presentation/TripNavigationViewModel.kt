package org.noztek.esktransport.feature.driver.trip_navigation.presentation

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.location.CurrentLocationProvider
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.core.realtime.driver.DriverBookingOfferRealtime
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPoint
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.CancelRiderTripUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.CompleteRiderTripUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.ConfirmRiderPickupUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.GetRiderTripSessionUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.SubmitRiderTripFeedbackUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.UpdateRiderTripLocationUseCase
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class TripNavigationUiEvent {
    data object NavigateToGoScreen : TripNavigationUiEvent()
}

class TripNavigationViewModel(
    private val getRiderTripSessionUseCase: GetRiderTripSessionUseCase,
    private val confirmRiderPickupUseCase: ConfirmRiderPickupUseCase,
    private val completeRiderTripUseCase: CompleteRiderTripUseCase,
    private val cancelRiderTripUseCase: CancelRiderTripUseCase,
    private val submitRiderTripFeedbackUseCase: SubmitRiderTripFeedbackUseCase,
    private val updateRiderTripLocationUseCase: UpdateRiderTripLocationUseCase,
    private val realtimeCoordinator: DriverBookingOfferRealtime,
    private val currentLocationProvider: CurrentLocationProvider,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripNavigationUiState())
    val uiState: StateFlow<TripNavigationUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<TripNavigationUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<TripNavigationUiEvent> = _uiEvents.asSharedFlow()
    private var realtimeJob: Job? = null
    private var lastPublishedLocation: DriverNavigationLocation? = null
    private var pendingLocation: DriverNavigationLocation? = null
    private var isPublishingLocation = false

    fun load(bookingPublicId: String) {
        if (bookingPublicId.isBlank()) {
            _uiState.value = TripNavigationUiState(
                isLoading = false,
                message = "Missing booking id.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            runCatching {
                val result = withContext(ioDispatcher) {
                    getRiderTripSessionUseCase(bookingPublicId)
                }
                result.onSuccess { session ->
                    _uiState.value = TripNavigationUiState(
                        isLoading = false,
                        tripSession = session,
                        isAtPickupPoint = session.status == "arriving_pickup",
                        isAtDestinationPoint = false,
                        nextInstruction = "Continue on route",
                    )
                    viewModelScope.launch {
                        runCatching {
                            val stageRoute = when (session.phase) {
                                RiderTripPhase.TO_PICKUP -> {
                                    val riderCurrent = session.riderCurrentPoint
                                    if (riderCurrent != null) {
                                        StageRoute(
                                            originLng = riderCurrent.longitude,
                                            originLat = riderCurrent.latitude,
                                            destinationLng = session.pickupPoint.longitude,
                                            destinationLat = session.pickupPoint.latitude,
                                        )
                                    } else {
                                        // Fallback while rider current location has not been persisted yet.
                                        StageRoute(
                                            originLng = session.pickupPoint.longitude,
                                            originLat = session.pickupPoint.latitude,
                                            destinationLng = session.pickupPoint.longitude,
                                            destinationLat = session.pickupPoint.latitude,
                                        )
                                    }
                                }
                                RiderTripPhase.TO_DESTINATION -> StageRoute(
                                    originLng = session.riderCurrentPoint?.longitude ?: session.pickupPoint.longitude,
                                    originLat = session.riderCurrentPoint?.latitude ?: session.pickupPoint.latitude,
                                    destinationLng = session.destinationPoint.longitude,
                                    destinationLat = session.destinationPoint.latitude,
                                )
                            }

                            val routeResult = withContext(ioDispatcher) {
                                mapboxDirectionsClient.getRoutePoints(
                                    originLongitude = stageRoute.originLng,
                                    originLatitude = stageRoute.originLat,
                                    destinationLongitude = stageRoute.destinationLng,
                                    destinationLatitude = stageRoute.destinationLat,
                                )
                            }
                            val summaryResult = withContext(ioDispatcher) {
                                mapboxDirectionsClient.getRouteSummary(
                                    originLongitude = stageRoute.originLng,
                                    originLatitude = stageRoute.originLat,
                                    destinationLongitude = stageRoute.destinationLng,
                                    destinationLatitude = stageRoute.destinationLat,
                                )
                            }
                            routeResult.onSuccess { routePoints ->
                                val summary = summaryResult.getOrNull()
                                _uiState.value = _uiState.value.copy(
                                    routePoints = routePoints,
                                    distanceMeters = summary?.distanceMeters,
                                    durationSeconds = summary?.durationSeconds,
                                )
                                publishCurrentLocationSnapshot(bookingPublicId)
                            }.onFailure {
                                _uiState.value = _uiState.value.copy(
                                    message = "Route loaded with fallback map data only.",
                                )
                                publishCurrentLocationSnapshot(bookingPublicId)
                            }
                        }.onFailure { throwable ->
                            _uiState.value = _uiState.value.copy(
                                message = throwable.message ?: "Failed to load route.",
                            )
                            publishCurrentLocationSnapshot(bookingPublicId)
                        }
                    }
                }.onFailure { error ->
                    _uiState.value = TripNavigationUiState(
                        isLoading = false,
                        message = error.message ?: "Failed to load trip session.",
                    )
                }
            }.onFailure { throwable ->
                _uiState.value = TripNavigationUiState(
                    isLoading = false,
                    message = throwable.message ?: "Unexpected trip loading error.",
                )
            }
        }
    }

    private fun publishCurrentLocationSnapshot(bookingPublicId: String) {
        if (bookingPublicId.isBlank()) return
        viewModelScope.launch {
            val currentLocation = withContext(ioDispatcher) {
                runCatching { currentLocationProvider.getLastKnownLocation() }.getOrNull()
            }
            if (currentLocation == null) {
                println("Driver trip current location snapshot unavailable.")
                return@launch
            }
            publishLocationIfNeeded(
                bookingPublicId = bookingPublicId,
                location = DriverNavigationLocation(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    bearing = null,
                    speedKph = null,
                    accuracyM = null,
                ),
            )
        }
    }

    fun startRealtime(bookingPublicId: String) {
        if (bookingPublicId.isBlank() || realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
            realtimeCoordinator.subscribeDriverBookingOffers()
            realtimeCoordinator.driverBookingCancelled().collect { event ->
                if (event.bookingPublicId != bookingPublicId || event.cancelledBy == "rider") return@collect
                _uiEvents.tryEmit(TripNavigationUiEvent.NavigateToGoScreen)
            }
        }
    }

    fun stopRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    fun confirmPickup(bookingPublicId: String) {
        if (bookingPublicId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingPickup = true, message = null)
            val result = withContext(ioDispatcher) {
                confirmRiderPickupUseCase(bookingPublicId)
            }
            result.onSuccess {
                val current = _uiState.value.tripSession
                if (current != null) {
                    _uiState.value = _uiState.value.copy(
                        tripSession = current.copy(
                            status = "in_progress",
                            phase = RiderTripPhase.TO_DESTINATION,
                        ),
                    )
                }
                load(bookingPublicId)
                _uiState.value = _uiState.value.copy(isSubmittingPickup = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingPickup = false,
                    message = error.message ?: "Failed to confirm pickup.",
                )
            }
        }
    }

    fun cancelTrip(bookingPublicId: String) {
        if (bookingPublicId.isBlank() || _uiState.value.isCancelling) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, message = null)
            val result = withContext(ioDispatcher) {
                cancelRiderTripUseCase(bookingPublicId)
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isCancelling = false)
                _uiEvents.tryEmit(TripNavigationUiEvent.NavigateToGoScreen)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCancelling = false,
                    message = error.message ?: "Failed to cancel trip.",
                )
            }
        }
    }

    fun completeTrip(bookingPublicId: String) {
        if (bookingPublicId.isBlank() || _uiState.value.isCompletingTrip) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompletingTrip = true, message = null)
            val result = withContext(ioDispatcher) {
                completeRiderTripUseCase(bookingPublicId)
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isCompletingTrip = false,
                    showFeedback = true,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCompletingTrip = false,
                    message = error.message ?: "Failed to complete trip.",
                )
            }
        }
    }

    fun submitFeedback(bookingPublicId: String, rating: Int, comment: String?) {
        if (bookingPublicId.isBlank() || _uiState.value.isSubmittingFeedback) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true, message = null)
            val result = withContext(ioDispatcher) {
                submitRiderTripFeedbackUseCase(
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
                _uiEvents.tryEmit(TripNavigationUiEvent.NavigateToGoScreen)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    message = error.message ?: "Failed to submit feedback.",
                )
            }
        }
    }

    fun skipFeedback() {
        _uiState.value = _uiState.value.copy(showFeedback = false)
        _uiEvents.tryEmit(TripNavigationUiEvent.NavigateToGoScreen)
    }

    fun publishLocationIfNeeded(
        bookingPublicId: String,
        location: DriverNavigationLocation,
    ) {
        if (bookingPublicId.isBlank()) return
        updateTripProximity(location)
        if (isPublishingLocation) {
            pendingLocation = location
            return
        }
        val previousLocation = lastPublishedLocation
        val enteredArrivalRadius = previousLocation != null &&
            !isWithinActiveTarget(previousLocation) &&
            isWithinActiveTarget(location)
        if (previousLocation != null &&
            previousLocation.distanceToMeters(location) < LOCATION_PUBLISH_DISTANCE_METERS &&
            !enteredArrivalRadius
        ) return

        val phase = when (_uiState.value.tripSession?.phase) {
            RiderTripPhase.TO_DESTINATION -> "to_destination"
            RiderTripPhase.TO_PICKUP,
            null -> "to_pickup"
        }

        isPublishingLocation = true
        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    updateRiderTripLocationUseCase(
                        bookingPublicId = bookingPublicId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        bearing = location.bearing,
                        speedKph = location.speedKph,
                        accuracyM = location.accuracyM,
                        recordedAt = location.recordedAt,
                        phase = phase,
                    )
                }
                result.onSuccess {
                    println(
                        "Driver trip location published bookingId=$bookingPublicId lat=${location.latitude} lng=${location.longitude} phase=$phase",
                    )
                    lastPublishedLocation = location
                }.onFailure { error ->
                    val message = error.message ?: "Failed to update driver location."
                    println("Driver trip location publish failed: $message")
                    _uiState.value = _uiState.value.copy(message = message)
                }
            } finally {
                isPublishingLocation = false
                val queuedLocation = pendingLocation
                pendingLocation = null
                if (queuedLocation != null) {
                    publishLocationIfNeeded(
                        bookingPublicId = bookingPublicId,
                        location = queuedLocation,
                    )
                }
            }
        }
    }

    private fun isWithinActiveTarget(location: DriverNavigationLocation): Boolean {
        val session = _uiState.value.tripSession ?: return false
        val targetPoint = when (session.phase) {
            RiderTripPhase.TO_PICKUP -> session.pickupPoint
            RiderTripPhase.TO_DESTINATION -> session.destinationPoint
        }
        return location.distanceToMeters(
            DriverNavigationLocation(
                latitude = targetPoint.latitude,
                longitude = targetPoint.longitude,
                bearing = null,
                speedKph = null,
                accuracyM = null,
            ),
        ) <= arrivalRadiusMeters(session.phase)
    }

    private fun updateTripProximity(location: DriverNavigationLocation) {
        val session = _uiState.value.tripSession ?: return
        val targetPoint = when (session.phase) {
            RiderTripPhase.TO_PICKUP -> session.pickupPoint
            RiderTripPhase.TO_DESTINATION -> session.destinationPoint
        }
        val targetLocation = DriverNavigationLocation(
            latitude = targetPoint.latitude,
            longitude = targetPoint.longitude,
            bearing = null,
            speedKph = null,
            accuracyM = null,
        )
        val isAtTarget = location.distanceToMeters(targetLocation) <= arrivalRadiusMeters(session.phase)
        val isAtPickup = session.phase == RiderTripPhase.TO_PICKUP && isAtTarget
        val isAtDestination = session.phase == RiderTripPhase.TO_DESTINATION && isAtTarget

        if (_uiState.value.isAtPickupPoint != isAtPickup || _uiState.value.isAtDestinationPoint != isAtDestination) {
            _uiState.value = _uiState.value.copy(
                isAtPickupPoint = isAtPickup,
                isAtDestinationPoint = isAtDestination,
            )
        }
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }
}

private data class StageRoute(
    val originLng: Double,
    val originLat: Double,
    val destinationLng: Double,
    val destinationLat: Double,
)

private const val LOCATION_PUBLISH_DISTANCE_METERS = 10.0
private const val PICKUP_ARRIVAL_RADIUS_METERS = 50.0
private const val DESTINATION_ARRIVAL_RADIUS_METERS = 15.0

private fun arrivalRadiusMeters(phase: RiderTripPhase): Double = when (phase) {
    RiderTripPhase.TO_PICKUP -> PICKUP_ARRIVAL_RADIUS_METERS
    RiderTripPhase.TO_DESTINATION -> DESTINATION_ARRIVAL_RADIUS_METERS
}

private fun RiderTripSession.isAtPickupPoint(): Boolean {
    if (status == "arriving_pickup") return true
    val current = riderCurrentPoint ?: return false
    if (phase != RiderTripPhase.TO_PICKUP) return false
    return current.distanceToMeters(pickupPoint) <= PICKUP_ARRIVAL_RADIUS_METERS
}

private fun RiderTripSession.isAtDestinationPoint(): Boolean {
    val current = riderCurrentPoint ?: return false
    if (phase != RiderTripPhase.TO_DESTINATION) return false
    return current.distanceToMeters(destinationPoint) <= DESTINATION_ARRIVAL_RADIUS_METERS
}

private fun DriverNavigationLocation.distanceToMeters(other: DriverNavigationLocation): Double {
    val earthRadiusMeters = 6_371_000.0
    val latitudeDelta = (other.latitude - latitude).toRadians()
    val longitudeDelta = (other.longitude - longitude).toRadians()
    val startLatitude = latitude.toRadians()
    val endLatitude = other.latitude.toRadians()
    val haversine = sin(latitudeDelta / 2.0) * sin(latitudeDelta / 2.0) +
        cos(startLatitude) * cos(endLatitude) * sin(longitudeDelta / 2.0) * sin(longitudeDelta / 2.0)
    val centralAngle = 2.0 * atan2(sqrt(haversine), sqrt(1.0 - haversine))
    return earthRadiusMeters * centralAngle
}

private fun RiderTripPoint.distanceToMeters(other: RiderTripPoint): Double {
    return DriverNavigationLocation(
        latitude = latitude,
        longitude = longitude,
        bearing = null,
        speedKph = null,
        accuracyM = null,
    ).distanceToMeters(
        DriverNavigationLocation(
            latitude = other.latitude,
            longitude = other.longitude,
            bearing = null,
            speedKph = null,
            accuracyM = null,
        ),
    )
}

private fun Double.toRadians(): Double = this * PI / 180.0
