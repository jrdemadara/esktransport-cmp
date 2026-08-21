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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingPayload
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverMarketplaceListingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverMarketplaceListingUseCase

data class MarketplaceListingEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val listing: DriverMarketplaceListing? = null,
    val title: String = "",
    val description: String = "",
    val baseRate: String = "",
    val rateUnit: String = "day",
    val minimumHours: String = "",
    val includedKm: String = "",
    val isAvailable: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

class MarketplaceListingEditViewModel(
    private val getDriverMarketplaceListingUseCase: GetDriverMarketplaceListingUseCase,
    private val updateDriverMarketplaceListingUseCase: UpdateDriverMarketplaceListingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceListingEditUiState())
    val uiState: StateFlow<MarketplaceListingEditUiState> = _uiState.asStateFlow()

    private var currentVehiclePublicId: String? = null
    private var currentServiceType: DriverVehicleServiceType? = null

    fun load(
        vehiclePublicId: String?,
        serviceType: DriverVehicleServiceType,
    ) {
        if (vehiclePublicId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Vehicle not found.",
                )
            }
            return
        }

        if (
            currentVehiclePublicId == vehiclePublicId &&
            currentServiceType == serviceType &&
            _uiState.value.listing != null
        ) {
            return
        }

        currentVehiclePublicId = vehiclePublicId
        currentServiceType = serviceType

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                getDriverMarketplaceListingUseCase(vehiclePublicId, serviceType)
            }
            result.fold(
                onSuccess = { listing ->
                    _uiState.update { listing.toUiState() }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load listing.",
                        )
                    }
                },
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onBaseRateChange(value: String) {
        _uiState.update { it.copy(baseRate = value) }
    }

    fun onRateUnitChange(value: String) {
        _uiState.update { it.copy(rateUnit = value) }
    }

    fun onMinimumHoursChange(value: String) {
        _uiState.update { it.copy(minimumHours = value) }
    }

    fun onIncludedKmChange(value: String) {
        _uiState.update { it.copy(includedKm = value) }
    }

    fun onAvailableChange(value: Boolean) {
        _uiState.update { it.copy(isAvailable = value) }
    }

    fun save() {
        updateListing(statusOverride = null, successMessage = "Listing saved.")
    }

    fun publish() {
        updateListing(statusOverride = "active", successMessage = "Listing published.")
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }

    private fun updateListing(
        statusOverride: String?,
        successMessage: String,
    ) {
        val state = _uiState.value
        val listing = state.listing ?: return
        if (state.isSaving) return

        val title = state.title.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Listing title is required.") }
            return
        }

        val payload = DriverMarketplaceListingPayload(
            title = title,
            description = state.description.trim().ifBlank { null },
            baseRate = state.baseRate.parseDecimalOrNull(),
            rateUnit = state.rateUnit,
            minimumHours = state.minimumHours.parseDecimalOrNull(),
            includedKm = state.includedKm.parseDecimalOrNull(),
            currency = listing.currency.ifBlank { "PHP" },
            status = statusOverride ?: listing.nextStatusForAvailability(state.isAvailable),
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                updateDriverMarketplaceListingUseCase(listing.publicId, payload)
            }
            result.fold(
                onSuccess = { updatedListing ->
                    _uiState.update {
                        updatedListing.toUiState().copy(
                            statusMessage = successMessage,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to update listing.",
                        )
                    }
                },
            )
        }
    }
}

private fun DriverMarketplaceListing.toUiState(): MarketplaceListingEditUiState {
    return MarketplaceListingEditUiState(
        isLoading = false,
        isSaving = false,
        listing = this,
        title = title,
        description = description.orEmpty(),
        baseRate = baseRate.formatDecimal(),
        rateUnit = rateUnit ?: "day",
        minimumHours = minimumHours.formatDecimal(),
        includedKm = includedKm.formatDecimal(),
        isAvailable = status == "active",
        errorMessage = null,
        statusMessage = null,
    )
}

private fun String.parseDecimalOrNull(): Double? {
    val normalized = replace("PHP", "", ignoreCase = true)
        .replace("₱", "")
        .replace(",", "")
        .trim()

    return normalized.ifBlank { null }?.toDoubleOrNull()
}

private fun Double?.formatDecimal(): String {
    val value = this ?: return ""
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun DriverMarketplaceListing.nextStatusForAvailability(isAvailable: Boolean): String {
    return when {
        isAvailable -> "active"
        status == "active" || status == "paused" -> "paused"
        else -> status
    }
}
