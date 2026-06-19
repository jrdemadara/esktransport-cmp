package org.noztek.esktransport.feature.driver.onboarding.presentation

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZone

data class DriverOnboardingUiState(
    val isLoading: Boolean = true,
    val isLoadingServiceZones: Boolean = false,
    val isSavingVehicle: Boolean = false,
    val isSubmittingIdentity: Boolean = false,
    val isSubmittingVehicleRegistration: Boolean = false,
    val isSubmittingServiceZones: Boolean = false,
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
    val serviceZones: List<DriverServiceZone> = emptyList(),
    val selectedServiceZoneIds: Set<Long> = emptySet(),
    val capturedPreviews: Map<DriverOnboardingDocumentType, CapturedDocumentPreview> = emptyMap(),
)

data class CapturedDocumentPreview(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CapturedDocumentPreview) return false

        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
