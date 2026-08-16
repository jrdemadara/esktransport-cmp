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
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentCategory
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReportPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentUrgency
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverIncidentReportsUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.SubmitDriverIncidentReportUseCase

data class DriverIncidentReportUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val category: DriverIncidentCategory = DriverIncidentCategory.Trip,
    val urgency: DriverIncidentUrgency = DriverIncidentUrgency.Normal,
    val bookingReference: String = "",
    val details: String = "",
    val reports: List<DriverIncidentReport> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val formErrorMessage: String? = null,
)

class DriverIncidentReportViewModel(
    private val getDriverIncidentReportsUseCase: GetDriverIncidentReportsUseCase,
    private val submitDriverIncidentReportUseCase: SubmitDriverIncidentReportUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverIncidentReportUiState())
    val uiState: StateFlow<DriverIncidentReportUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverIncidentReportsUseCase() }
            result.fold(
                onSuccess = { reports ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reports = reports,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load incident reports.",
                        )
                    }
                },
            )
        }
    }

    fun selectCategory(category: DriverIncidentCategory) {
        _uiState.update { it.copy(category = category, formErrorMessage = null) }
    }

    fun selectUrgency(urgency: DriverIncidentUrgency) {
        _uiState.update { it.copy(urgency = urgency, formErrorMessage = null) }
    }

    fun updateBookingReference(value: String) {
        _uiState.update { it.copy(bookingReference = value, formErrorMessage = null) }
    }

    fun updateDetails(value: String) {
        _uiState.update { it.copy(details = value, formErrorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val validationError = state.validationError()
        if (validationError != null) {
            _uiState.update { it.copy(formErrorMessage = validationError) }
            return
        }
        if (state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) {
                submitDriverIncidentReportUseCase(
                    DriverIncidentReportPayload(
                        category = state.category,
                        urgency = state.urgency,
                        bookingReference = state.bookingReference,
                        details = state.details,
                    ),
                )
            }
            result.fold(
                onSuccess = { reports ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            bookingReference = "",
                            details = "",
                            reports = reports,
                            formErrorMessage = null,
                            successMessage = "Incident report submitted.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "Unable to submit incident report.",
                        )
                    }
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun DriverIncidentReportUiState.validationError(): String? {
        return when {
            details.isBlank() -> "Describe what happened."
            details.trim().length < 10 -> "Add a few more details before submitting."
            else -> null
        }
    }
}
