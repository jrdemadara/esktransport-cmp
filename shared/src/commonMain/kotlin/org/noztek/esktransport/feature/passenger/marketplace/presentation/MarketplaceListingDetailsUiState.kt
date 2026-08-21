package org.noztek.esktransport.feature.passenger.marketplace.presentation

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing

data class MarketplaceListingDetailsUiState(
    val isLoading: Boolean = false,
    val listing: MarketplaceListing? = null,
    val vehiclePhotoBytes: ByteArray? = null,
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarketplaceListingDetailsUiState) return false

        return isLoading == other.isLoading &&
            listing == other.listing &&
            vehiclePhotoBytes.contentEquals(other.vehiclePhotoBytes) &&
            errorMessage == other.errorMessage
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (listing?.hashCode() ?: 0)
        result = 31 * result + (vehiclePhotoBytes?.contentHashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
