package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.ConfirmRiderPickupUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.GetRiderTripSessionUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.UpdateRiderTripLocationUseCase

class TripNavigationViewModel(
    private val getRiderTripSessionUseCase: GetRiderTripSessionUseCase,
    private val confirmRiderPickupUseCase: ConfirmRiderPickupUseCase,
    private val updateRiderTripLocationUseCase: UpdateRiderTripLocationUseCase,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripNavigationUiState())
    val uiState: StateFlow<TripNavigationUiState> = _uiState.asStateFlow()

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
                                    routePoints = routePoints,
                                    distanceMeters = summary?.distanceMeters,
                                    durationSeconds = summary?.durationSeconds,
                                    nextInstruction = "Continue on route",
                                )
                            }.onFailure {
                                _uiState.value = TripNavigationUiState(
                                    isLoading = false,
                                    tripSession = session,
                                    message = "Route loaded with fallback map data only.",
                                )
                            }
                        }.onFailure { throwable ->
                            _uiState.value = TripNavigationUiState(
                                isLoading = false,
                                tripSession = session,
                                message = throwable.message ?: "Failed to load route.",
                            )
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
                        tripSession = current.copy(phase = RiderTripPhase.TO_DESTINATION),
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

    fun publishLocation(
        bookingPublicId: String,
        latitude: Double,
        longitude: Double,
        bearing: Double?,
        speedKph: Double?,
        accuracyM: Double?,
        phase: String,
    ) {
        if (bookingPublicId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                withContext(ioDispatcher) {
                    updateRiderTripLocationUseCase(
                        bookingPublicId = bookingPublicId,
                        latitude = latitude,
                        longitude = longitude,
                        bearing = bearing,
                        speedKph = speedKph,
                        accuracyM = accuracyM,
                        phase = phase,
                    )
                }
            }
        }
    }
}

private data class StageRoute(
    val originLng: Double,
    val originLat: Double,
    val destinationLng: Double,
    val destinationLat: Double,
)
