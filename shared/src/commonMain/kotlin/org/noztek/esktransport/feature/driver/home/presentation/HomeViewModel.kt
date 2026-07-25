package org.noztek.esktransport.feature.driver.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.core.realtime.model.displayMessage
import org.noztek.esktransport.core.realtime.model.matchesDriver
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard
import org.noztek.esktransport.feature.driver.earning.domain.usecase.GetRiderEarningsUseCase
import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.home.domain.usecase.GetDriverHomeStatsUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.ObserveDriverOnboardingStatusChangedUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UnsubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTrip
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripStatus
import org.noztek.esktransport.feature.driver.trips.domain.usecase.GetDriverTripsUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

data class HomeUiState(
    val userName: String? = null,
    val isLoadingSetup: Boolean = true,
    val isLoadingStats: Boolean = true,
    val isLoadingWallet: Boolean = true,
    val isLoadingEarnings: Boolean = true,
    val isLoadingRecentActivity: Boolean = true,
    val stats: DriverHomeStats? = null,
    val earningsDashboard: RiderEarningsDashboard? = null,
    val recentActivityTrip: DriverTrip? = null,
    val recentActivityRoutePoints: List<MapPoint> = emptyList(),
    val onboardingStatus: DriverOnboardingStatus? = null,
    val walletDashboard: DriverWalletDashboard? = null,
    val errorMessage: String? = null,
    val statsErrorMessage: String? = null,
    val walletErrorMessage: String? = null,
    val earningsErrorMessage: String? = null,
    val recentActivityErrorMessage: String? = null,
    val statusMessage: String? = null,
)

class HomeViewModel(
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val observeDriverOnboardingStatusChangedUseCase: ObserveDriverOnboardingStatusChangedUseCase,
    private val subscribeDriverOnboardingRealtimeUseCase: SubscribeDriverOnboardingRealtimeUseCase,
    private val unsubscribeDriverOnboardingRealtimeUseCase: UnsubscribeDriverOnboardingRealtimeUseCase,
    private val getDriverHomeStatsUseCase: GetDriverHomeStatsUseCase,
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val getRiderEarningsUseCase: GetRiderEarningsUseCase,
    private val getDriverTripsUseCase: GetDriverTripsUseCase,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        observeDriverOnboardingRealtime()
        refreshOnboardingStatus()
        refreshStats()
        refreshWallet()
        refreshEarnings()
        refreshRecentActivity()
    }

    fun refreshOnboardingStatus(
        showLoading: Boolean = true,
        statusMessage: String? = null,
    ) {
        if (!showLoading && _uiState.value.isLoadingSetup) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingSetup = showLoading,
                    errorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { getDriverOnboardingStatusUseCase() }
            result.fold(
                onSuccess = { status ->
                    subscribeDriverOnboardingRealtimeUseCase(status.driverId)
                    _uiState.update {
                        it.copy(
                            isLoadingSetup = false,
                            onboardingStatus = status,
                            errorMessage = null,
                            statusMessage = statusMessage,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingSetup = false,
                            errorMessage = throwable.message ?: "Unable to load driver setup.",
                        )
                    }
                },
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun refreshWallet(showLoading: Boolean = true) {
        if (!showLoading && _uiState.value.isLoadingWallet) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingWallet = showLoading,
                    walletErrorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { getDriverWalletUseCase() }
            result.fold(
                onSuccess = { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoadingWallet = false,
                            walletDashboard = dashboard,
                            walletErrorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingWallet = false,
                            walletErrorMessage = throwable.message ?: "Unable to load wallet.",
                        )
                    }
                },
            )
        }
    }

    fun refreshStats(showLoading: Boolean = true) {
        if (!showLoading && _uiState.value.isLoadingStats) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingStats = showLoading,
                    statsErrorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { getDriverHomeStatsUseCase() }
            result.fold(
                onSuccess = { stats ->
                    _uiState.update {
                        it.copy(
                            isLoadingStats = false,
                            stats = stats,
                            statsErrorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingStats = false,
                            statsErrorMessage = throwable.message ?: "Unable to load driver stats.",
                        )
                    }
                },
            )
        }
    }

    fun refreshEarnings(showLoading: Boolean = true) {
        if (!showLoading && _uiState.value.isLoadingEarnings) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingEarnings = showLoading,
                    earningsErrorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { getRiderEarningsUseCase() }
            result.fold(
                onSuccess = { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoadingEarnings = false,
                            earningsDashboard = dashboard,
                            earningsErrorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingEarnings = false,
                            earningsErrorMessage = throwable.message ?: "Unable to load earnings.",
                        )
                    }
                },
            )
        }
    }

    fun refreshRecentActivity(showLoading: Boolean = true) {
        if (!showLoading && _uiState.value.isLoadingRecentActivity) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingRecentActivity = showLoading,
                    recentActivityErrorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { getDriverTripsUseCase() }
            result.fold(
                onSuccess = { dashboard ->
                    val recentTrip = dashboard.trips.latestRecentTrip()
                    val routePoints = recentTrip?.recentNavigationRoutePoints().orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoadingRecentActivity = false,
                            recentActivityTrip = recentTrip,
                            recentActivityRoutePoints = routePoints,
                            recentActivityErrorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingRecentActivity = false,
                            recentActivityErrorMessage = throwable.message ?: "Unable to load recent activity.",
                        )
                    }
                },
            )
        }
    }

    override fun onCleared() {
        unsubscribeDriverOnboardingRealtimeUseCase()
        super.onCleared()
    }

    private fun observeDriverOnboardingRealtime() {
        viewModelScope.launch {
            observeDriverOnboardingStatusChangedUseCase().collect { event ->
                val currentDriverId = _uiState.value.onboardingStatus?.driverId
                if (event.matchesDriver(currentDriverId)) {
                    refreshOnboardingStatus(
                        showLoading = false,
                        statusMessage = event.displayMessage(),
                    )
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeCurrentSessionUseCase().collect { user ->
                _uiState.update { it.copy(userName = user.name) }
            }
        }
    }

    private fun List<DriverTrip>.latestRecentTrip(): DriverTrip? {
        return sortedWith(
            compareByDescending<DriverTrip> {
                when (it.status) {
                    DriverTripStatus.Completed -> 3
                    DriverTripStatus.InProgress,
                    DriverTripStatus.ArrivingPickup,
                    DriverTripStatus.Accepted,
                    DriverTripStatus.Offered -> 2
                    DriverTripStatus.Cancelled,
                    DriverTripStatus.Expired -> 1
                    DriverTripStatus.Unknown -> 0
                }
            }.thenByDescending { it.activityTimestamp().orEmpty() },
        ).firstOrNull()
    }

    private fun DriverTrip.activityTimestamp(): String? {
        return completedAt
            ?: canceledAt
            ?: pickupConfirmedAt
            ?: acceptedAt
            ?: assignedAt
            ?: requestedAt
    }

    private suspend fun DriverTrip.recentNavigationRoutePoints(): List<MapPoint> {
        val pickupLatitude = pickup.lat ?: return emptyList()
        val pickupLongitude = pickup.lng ?: return emptyList()
        val dropoffLatitude = dropoff.lat ?: return emptyList()
        val dropoffLongitude = dropoff.lng ?: return emptyList()

        return mapboxDirectionsClient.getRoutePoints(
            originLongitude = pickupLongitude,
            originLatitude = pickupLatitude,
            destinationLongitude = dropoffLongitude,
            destinationLatitude = dropoffLatitude,
        ).getOrElse {
            listOf(
                MapPoint(latitude = pickupLatitude, longitude = pickupLongitude),
                MapPoint(latitude = dropoffLatitude, longitude = dropoffLongitude),
            )
        }
    }
}
