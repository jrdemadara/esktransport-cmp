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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.ObserveDriverOnboardingStatusChangedUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UnsubscribeDriverOnboardingRealtimeUseCase

data class HomeUiState(
    val isLoadingSetup: Boolean = true,
    val onboardingStatus: DriverOnboardingStatus? = null,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val observeDriverOnboardingStatusChangedUseCase: ObserveDriverOnboardingStatusChangedUseCase,
    private val subscribeDriverOnboardingRealtimeUseCase: SubscribeDriverOnboardingRealtimeUseCase,
    private val unsubscribeDriverOnboardingRealtimeUseCase: UnsubscribeDriverOnboardingRealtimeUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDriverOnboardingRealtime()
        refreshOnboardingStatus()
    }

    fun refreshOnboardingStatus(showLoading: Boolean = true) {
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

    override fun onCleared() {
        unsubscribeDriverOnboardingRealtimeUseCase()
        super.onCleared()
    }

    private fun observeDriverOnboardingRealtime() {
        viewModelScope.launch {
            observeDriverOnboardingStatusChangedUseCase().collect { event ->
                val currentDriverId = _uiState.value.onboardingStatus?.driverId
                if (currentDriverId == null || currentDriverId == event.driverId) {
                    refreshOnboardingStatus(showLoading = false)
                }
            }
        }
    }
}
