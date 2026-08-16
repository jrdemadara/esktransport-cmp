package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZone
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZoneSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverServiceZonesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverServiceZonesUseCase

data class DriverServiceAreasUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val zones: List<DriverServiceZone> = emptyList(),
    val selectedZoneIds: Set<Long> = emptySet(),
    val savedZoneIds: Set<Long> = emptySet(),
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val hasChanges: Boolean
        get() = selectedZoneIds != savedZoneIds
}

class DriverServiceAreasViewModel(
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val getDriverServiceZonesUseCase: GetDriverServiceZonesUseCase,
    private val submitDriverServiceZonesUseCase: SubmitDriverServiceZonesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverServiceAreasUiState())
    val uiState: StateFlow<DriverServiceAreasUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }

            val (zonesResult, statusResult) = withContext(ioDispatcher) {
                coroutineScope {
                    val zonesDeferred = async { getDriverServiceZonesUseCase() }
                    val statusDeferred = async { getDriverOnboardingStatusUseCase() }
                    zonesDeferred.await() to statusDeferred.await()
                }
            }

            val zones = zonesResult.getOrNull().orEmpty()
            val savedZoneIds = statusResult.getOrNull()
                ?.serviceZones
                ?.map { it.id }
                ?.toSet()
                .orEmpty()
            val error = zonesResult.exceptionOrNull()?.message
                ?: statusResult.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    isLoading = false,
                    zones = zones,
                    selectedZoneIds = savedZoneIds,
                    savedZoneIds = savedZoneIds,
                    errorMessage = error,
                )
            }
        }
    }

    fun toggleZone(zoneId: Long) {
        _uiState.update { state ->
            val selected = state.selectedZoneIds
            state.copy(
                selectedZoneIds = if (zoneId in selected) {
                    selected - zoneId
                } else {
                    selected + zoneId
                },
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun save() {
        val selectedIds = _uiState.value.selectedZoneIds.toList()
        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Choose at least one service area.") }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                submitDriverServiceZonesUseCase(
                    DriverServiceZoneSelectionPayload(serviceZoneIds = selectedIds),
                )
            }
            result.fold(
                onSuccess = { status ->
                    val savedIds = status.serviceZones.map { it.id }.toSet()
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            selectedZoneIds = savedIds,
                            savedZoneIds = savedIds,
                            statusMessage = "Service areas updated.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save service areas.",
                        )
                    }
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }
}
