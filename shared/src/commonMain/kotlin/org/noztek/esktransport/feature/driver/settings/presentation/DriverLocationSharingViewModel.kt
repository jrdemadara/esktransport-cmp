package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettingsPayload
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverLocationSharingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverLocationSharingUseCase

data class DriverLocationSharingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val settings: DriverLocationSharingSettings? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class DriverLocationSharingViewModel(
    private val getDriverLocationSharingUseCase: GetDriverLocationSharingUseCase,
    private val updateDriverLocationSharingUseCase: UpdateDriverLocationSharingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverLocationSharingUiState())
    val uiState: StateFlow<DriverLocationSharingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverLocationSharingUseCase() }
            result.fold(
                onSuccess = { settings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = settings,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load location sharing settings.",
                        )
                    }
                },
            )
        }
    }

    fun setSupportLocationEnabled(enabled: Boolean) {
        updateSettings { it.copy(supportLocationEnabled = enabled) }
    }

    fun setIncidentLocationEnabled(enabled: Boolean) {
        updateSettings { it.copy(incidentLocationEnabled = enabled) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun updateSettings(transform: (DriverLocationSharingSettings) -> DriverLocationSharingSettings) {
        val current = _uiState.value.settings ?: return
        if (_uiState.value.isSaving) return
        val updated = transform(current)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    settings = updated,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                updateDriverLocationSharingUseCase(
                    DriverLocationSharingSettingsPayload(
                        supportLocationEnabled = updated.supportLocationEnabled,
                        incidentLocationEnabled = updated.incidentLocationEnabled,
                    ),
                )
            }
            result.fold(
                onSuccess = { settings ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settings = settings,
                            successMessage = "Location sharing settings saved.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            settings = current,
                            errorMessage = throwable.message ?: "Unable to update location sharing settings.",
                        )
                    }
                },
            )
        }
    }
}
