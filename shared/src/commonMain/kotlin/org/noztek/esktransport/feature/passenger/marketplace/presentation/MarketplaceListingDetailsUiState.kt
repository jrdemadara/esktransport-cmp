package org.noztek.esktransport.feature.passenger.marketplace.presentation

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest

data class MarketplaceListingDetailsUiState(
    val isLoading: Boolean = false,
    val listing: MarketplaceListing? = null,
    val vehiclePhotoBytes: ByteArray? = null,
    val isVehiclePhotoLoading: Boolean = false,
    val requestedStartAt: String = "",
    val requestedEndAt: String = "",
    val pickupLabel: String = "",
    val destinationLabel: String = "",
    val passengerCount: String = "",
    val cargoDescription: String = "",
    val cargoWeightKg: String = "",
    val cargoPieces: String = "",
    val customerNote: String = "",
    val isSendingRequest: Boolean = false,
    val sentRequestPublicId: String? = null,
    val serviceRequest: MarketplaceServiceRequest? = null,
    val isUpdatingRequest: Boolean = false,
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarketplaceListingDetailsUiState) return false

        return isLoading == other.isLoading &&
            listing == other.listing &&
            vehiclePhotoBytes.contentEquals(other.vehiclePhotoBytes) &&
            isVehiclePhotoLoading == other.isVehiclePhotoLoading &&
            requestedStartAt == other.requestedStartAt &&
            requestedEndAt == other.requestedEndAt &&
            pickupLabel == other.pickupLabel &&
            destinationLabel == other.destinationLabel &&
            passengerCount == other.passengerCount &&
            cargoDescription == other.cargoDescription &&
            cargoWeightKg == other.cargoWeightKg &&
            cargoPieces == other.cargoPieces &&
            customerNote == other.customerNote &&
            isSendingRequest == other.isSendingRequest &&
            sentRequestPublicId == other.sentRequestPublicId &&
            errorMessage == other.errorMessage
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (listing?.hashCode() ?: 0)
        result = 31 * result + (vehiclePhotoBytes?.contentHashCode() ?: 0)
        result = 31 * result + isVehiclePhotoLoading.hashCode()
        result = 31 * result + requestedStartAt.hashCode()
        result = 31 * result + requestedEndAt.hashCode()
        result = 31 * result + pickupLabel.hashCode()
        result = 31 * result + destinationLabel.hashCode()
        result = 31 * result + passengerCount.hashCode()
        result = 31 * result + cargoDescription.hashCode()
        result = 31 * result + cargoWeightKg.hashCode()
        result = 31 * result + cargoPieces.hashCode()
        result = 31 * result + customerNote.hashCode()
        result = 31 * result + isSendingRequest.hashCode()
        result = 31 * result + (sentRequestPublicId?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
