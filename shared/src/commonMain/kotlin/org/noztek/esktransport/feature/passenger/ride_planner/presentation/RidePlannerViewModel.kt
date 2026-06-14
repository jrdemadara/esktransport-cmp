package org.noztek.esktransport.feature.passenger.ride_planner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.NearbyDriver
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailability
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.GetNearbyDriversUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.GetRouteUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.ResolveCurrentLocationPointUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.ResolveCurrentLocationLabelUseCase

sealed class RidePlannerUiEvent {
    data class ShowSnackbar(val message: String) : RidePlannerUiEvent()
    data object NavigateToBookingReview : RidePlannerUiEvent()
}

class RidePlannerViewModel(
    private val resolveCurrentLocationLabelUseCase: ResolveCurrentLocationLabelUseCase,
    private val resolveCurrentLocationPointUseCase: ResolveCurrentLocationPointUseCase,
    private val getNearbyDriversUseCase: GetNearbyDriversUseCase,
    private val getRouteUseCase: GetRouteUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _pickupLocation = MutableStateFlow("")
    val pickupLocation: StateFlow<String> = _pickupLocation.asStateFlow()
    
    private val _pickupPoint = MutableStateFlow<GeoPoint?>(null)
    val pickupPoint: StateFlow<GeoPoint?> = _pickupPoint.asStateFlow()

    private val _destinationLocation = MutableStateFlow("")
    val destinationLocation: StateFlow<String> = _destinationLocation.asStateFlow()
    
    private val _destinationPoint = MutableStateFlow<GeoPoint?>(null)
    val destinationPoint: StateFlow<GeoPoint?> = _destinationPoint.asStateFlow()

    private val _uiEvents = MutableSharedFlow<RidePlannerUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<RidePlannerUiEvent> = _uiEvents.asSharedFlow()

    private val _availability = MutableStateFlow<RideAvailability?>(null)
    val availability: StateFlow<RideAvailability?> = _availability.asStateFlow()
    private val _isLoadingAvailability = MutableStateFlow(false)
    val isLoadingAvailability: StateFlow<Boolean> = _isLoadingAvailability.asStateFlow()

    private val _selectedDriver = MutableStateFlow<NearbyDriver?>(null)
    val selectedDriver: StateFlow<NearbyDriver?> = _selectedDriver.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    private val _passengerCount = MutableStateFlow(0) // 0 index for "1"
    val passengerCount: StateFlow<Int> = _passengerCount.asStateFlow()

    private val _selectedVehicleType = MutableStateFlow(0) // 0: Moto, 1: Trike, 2: Car, 3: Van
    val selectedVehicleType: StateFlow<Int> = _selectedVehicleType.asStateFlow()

    suspend fun resolveCurrentLocationLabel(): String? = withContext(ioDispatcher) {
        resolveCurrentLocationLabelUseCase()
    }

    suspend fun resolveCurrentLocationPoint(): GeoPoint? = withContext(ioDispatcher) {
        resolveCurrentLocationPointUseCase()
    }

    fun setPickupLocation(value: String, point: GeoPoint?) {
        _pickupLocation.value = value
        _pickupPoint.value = point
        _routePoints.value = emptyList()
    }

    fun setDestinationLocation(value: String, point: GeoPoint?) {
        _destinationLocation.value = value
        _destinationPoint.value = point
        _routePoints.value = emptyList()
    }

    fun setPassengerCount(index: Int) {
        _passengerCount.value = index.coerceIn(MIN_PASSENGER_INDEX, MAX_PASSENGER_INDEX)
    }

    fun incrementPassengerCount() {
        setPassengerCount(_passengerCount.value + 1)
    }

    fun decrementPassengerCount() {
        setPassengerCount(_passengerCount.value - 1)
    }

    fun setVehicleType(index: Int) {
        _selectedVehicleType.value = index
    }

    fun onReviewBookingClick() {
        val pickup = _pickupPoint.value
        val destination = _destinationPoint.value
        if (pickup != null && destination != null) {
            viewModelScope.launch {
                val origin = GeoPoint(pickup.latitude, pickup.longitude)
                val destinationPoint = GeoPoint(destination.latitude, destination.longitude)
                getRouteUseCase(origin, destinationPoint).onSuccess { points ->
                    _routePoints.value = points
                    _uiEvents.tryEmit(RidePlannerUiEvent.NavigateToBookingReview)
                }.onFailure {
                    // Fallback to straight line if route fails
                    _routePoints.value = listOf(origin, destinationPoint)
                    _uiEvents.tryEmit(RidePlannerUiEvent.NavigateToBookingReview)
                }
            }
        } else {
            _uiEvents.tryEmit(RidePlannerUiEvent.NavigateToBookingReview)
        }
    }

    fun onDriverCardClick(driver: NearbyDriver) {
        // This is now deprecated as driver cards are removed from UI
    }

    companion object {
        private const val MIN_PASSENGER_INDEX = 0
        private const val MAX_PASSENGER_INDEX = 4
    }
}
