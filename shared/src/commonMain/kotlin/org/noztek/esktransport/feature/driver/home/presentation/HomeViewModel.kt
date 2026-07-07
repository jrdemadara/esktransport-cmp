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
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

data class HomeUiState(
    val userName: String? = null,
    val isLoadingSetup: Boolean = true,
    val isLoadingStats: Boolean = true,
    val isLoadingWallet: Boolean = true,
    val isLoadingEarnings: Boolean = true,
    val isCreatingTopup: Boolean = false,
    val stats: DriverHomeStats? = null,
    val earningsDashboard: RiderEarningsDashboard? = null,
    val onboardingStatus: DriverOnboardingStatus? = null,
    val walletDashboard: DriverWalletDashboard? = null,
    val selectedTopup: DriverWalletTopup? = null,
    val errorMessage: String? = null,
    val statsErrorMessage: String? = null,
    val walletErrorMessage: String? = null,
    val earningsErrorMessage: String? = null,
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
    private val createDriverTopupUseCase: CreateDriverTopupUseCase,
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

    fun createTopup(amount: Double) {
        if (_uiState.value.isCreatingTopup) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingTopup = true,
                    walletErrorMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                createDriverTopupUseCase(amount = amount)
            }
            result.fold(
                onSuccess = { topup ->
                    _uiState.update {
                        val currentDashboard = it.walletDashboard
                        it.copy(
                            isCreatingTopup = false,
                            selectedTopup = topup,
                            walletDashboard = currentDashboard?.copy(
                                pendingTopups = listOf(topup) + currentDashboard.pendingTopups.filterNot { pending ->
                                    pending.referenceCode == topup.referenceCode
                                },
                            ),
                            walletErrorMessage = null,
                            statusMessage = "Top-up reference created.",
                        )
                    }
                    refreshWallet(showLoading = false)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isCreatingTopup = false,
                            walletErrorMessage = throwable.message ?: "Unable to create top-up reference.",
                        )
                    }
                },
            )
        }
    }

    fun clearSelectedTopup() {
        _uiState.update { it.copy(selectedTopup = null) }
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
}
