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
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.GetRiderTripSessionUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.UpdateRiderTripLocationUseCase

class TripNavigationViewModel(
    private val getRiderTripSessionUseCase: GetRiderTripSessionUseCase,
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
                            val routeResult = withContext(ioDispatcher) {
                                mapboxDirectionsClient.getRoutePoints(
                                    originLongitude = session.pickupPoint.longitude,
                                    originLatitude = session.pickupPoint.latitude,
                                    destinationLongitude = session.destinationPoint.longitude,
                                    destinationLatitude = session.destinationPoint.latitude,
                                )
                            }
                            routeResult.onSuccess { routePoints ->
                                _uiState.value = TripNavigationUiState(
                                    isLoading = false,
                                    tripSession = session,
                                    routePoints = routePoints,
                                    distanceMeters = null,
                                    durationSeconds = null,
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
