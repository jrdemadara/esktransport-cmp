package org.noztek.esktransport.feature.driver.onboarding.presentation

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus

data class DriverOnboardingUiState(
    val isLoading: Boolean = true,
    val isSavingVehicle: Boolean = false,
    val isSubmitting: Boolean = false,
    val uploadingType: DriverOnboardingDocumentType? = null,
    val status: DriverOnboardingStatus? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val licenseNo: String = "",
    val licenseExpiry: String = "",
    val vehicleTypeCode: String = "motorcycle",
    val plate: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val passengerCapacity: String = "",
)
