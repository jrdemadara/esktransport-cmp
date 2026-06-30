package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.CancelRiderTripUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.ConfirmRiderPickupUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.GetRiderTripSessionUseCase
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
    private val cancelRiderTripUseCase: CancelRiderTripUseCase,
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
    private var mockLocationJob: Job? = null
    private var lastPublishedLocation: DriverNavigationLocation? = null
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
                    startMockLocationUpdates(
                        bookingPublicId = bookingPublicId,
                        session = session,
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
                                    originLng = session.pickupPoint.longitude,
                                    originLat = session.pickupPoint.latitude,
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
                                _uiState.value = TripNavigationUiState(
                                    isLoading = false,
                                    tripSession = session,
                                    isAtPickupPoint = session.status == "arriving_pickup",
                                    routePoints = routePoints,
                                    distanceMeters = summary?.distanceMeters,
                                    durationSeconds = summary?.durationSeconds,
                                    nextInstruction = "Continue on route",
                                )
                                publishCurrentLocationSnapshot(bookingPublicId)
                            }.onFailure {
                                _uiState.value = TripNavigationUiState(
                                    isLoading = false,
                                    tripSession = session,
                                    isAtPickupPoint = session.status == "arriving_pickup",
                                    message = "Route loaded with fallback map data only.",
                                )
                                publishCurrentLocationSnapshot(bookingPublicId)
                            }
                        }.onFailure { throwable ->
                            _uiState.value = TripNavigationUiState(
                                isLoading = false,
                                tripSession = session,
                                isAtPickupPoint = session.status == "arriving_pickup",
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
        mockLocationJob?.cancel()
        mockLocationJob = null
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

    fun publishLocationIfNeeded(
        bookingPublicId: String,
        location: DriverNavigationLocation,
    ) {
        if (bookingPublicId.isBlank()) return
        updatePickupProximity(location)
        if (isPublishingLocation) return
        val previousLocation = lastPublishedLocation
        if (previousLocation != null && previousLocation.distanceToMeters(location) < LOCATION_PUBLISH_DISTANCE_METERS) return

        val phase = when (_uiState.value.tripSession?.phase) {
            RiderTripPhase.TO_DESTINATION -> "to_destination"
            RiderTripPhase.TO_PICKUP,
            null -> "to_pickup"
        }

        viewModelScope.launch {
            isPublishingLocation = true
            try {
                val result = withContext(ioDispatcher) {
                    updateRiderTripLocationUseCase(
                        bookingPublicId = bookingPublicId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        bearing = location.bearing,
                        speedKph = location.speedKph,
                        accuracyM = location.accuracyM,
                        phase = phase,
                    )
                }
                result.onSuccess {
                    println(
                        "Driver trip location published bookingId=$bookingPublicId lat=${location.latitude} lng=${location.longitude} phase=$phase",
                    )
                    lastPublishedLocation = location
                }.onFailure { error ->
                    println("Driver trip location publish failed: ${error.message}")
                }
            } finally {
                isPublishingLocation = false
            }
        }
    }

    private fun updatePickupProximity(location: DriverNavigationLocation) {
        val session = _uiState.value.tripSession ?: return
        if (session.phase != RiderTripPhase.TO_PICKUP) {
            if (_uiState.value.isAtPickupPoint) {
                _uiState.value = _uiState.value.copy(isAtPickupPoint = false)
            }
            return
        }

        val pickupLocation = DriverNavigationLocation(
            latitude = session.pickupPoint.latitude,
            longitude = session.pickupPoint.longitude,
            bearing = null,
            speedKph = null,
            accuracyM = null,
        )
        val isAtPickup = location.distanceToMeters(pickupLocation) <= PICKUP_CONFIRM_DISTANCE_METERS
        if (_uiState.value.isAtPickupPoint != isAtPickup) {
            _uiState.value = _uiState.value.copy(isAtPickupPoint = isAtPickup)
        }
    }

    private fun startMockLocationUpdates(
        bookingPublicId: String,
        session: RiderTripSession,
    ) {
        if (!ENABLE_MOCK_DRIVER_LOCATION_UPDATES || bookingPublicId.isBlank()) return
        mockLocationJob?.cancel()
        mockLocationJob = viewModelScope.launch {
            var tick = 0
            while (true) {
                val activeSession = _uiState.value.tripSession ?: session
                publishMockLocation(
                    bookingPublicId = bookingPublicId,
                    location = activeSession.mockDriverLocation(tick),
                    phase = activeSession.phase,
                    tick = tick,
                )
                tick += 1
                delay(MOCK_DRIVER_LOCATION_INTERVAL_MS)
            }
        }
    }

    private suspend fun publishMockLocation(
        bookingPublicId: String,
        location: DriverNavigationLocation,
        phase: RiderTripPhase,
        tick: Int,
    ) {
        val phaseValue = when (phase) {
            RiderTripPhase.TO_DESTINATION -> "to_destination"
            RiderTripPhase.TO_PICKUP -> "to_pickup"
        }
        val result = withContext(ioDispatcher) {
            updateRiderTripLocationUseCase(
                bookingPublicId = bookingPublicId,
                latitude = location.latitude,
                longitude = location.longitude,
                bearing = location.bearing,
                speedKph = location.speedKph,
                accuracyM = location.accuracyM,
                phase = phaseValue,
            )
        }
        result.onSuccess {
            println(
                "Mock driver trip location published tick=$tick bookingId=$bookingPublicId lat=${location.latitude} lng=${location.longitude} phase=$phaseValue",
            )
            lastPublishedLocation = location
        }.onFailure { error ->
            println("Mock driver trip location publish failed tick=$tick: ${error.message}")
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
private const val PICKUP_CONFIRM_DISTANCE_METERS = 40.0
private const val ENABLE_MOCK_DRIVER_LOCATION_UPDATES = false
private const val MOCK_DRIVER_LOCATION_INTERVAL_MS = 5_000L

private fun RiderTripSession.mockDriverLocation(tick: Int): DriverNavigationLocation {
    val source = when (phase) {
        RiderTripPhase.TO_DESTINATION -> destinationPoint
        RiderTripPhase.TO_PICKUP -> pickupPoint
    }
    val step = tick % 12
    val latitudeOffset = -0.00055 + (step * 0.00010)
    val longitudeOffset = when (step % 4) {
        0 -> -0.00018
        1 -> -0.00006
        2 -> 0.00006
        else -> 0.00018
    }
    return DriverNavigationLocation(
        latitude = source.latitude + latitudeOffset,
        longitude = source.longitude + longitudeOffset,
        bearing = null,
        speedKph = 18.0,
        accuracyM = 6.0,
    )
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

private fun Double.toRadians(): Double = this * PI / 180.0
