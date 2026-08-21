package org.noztek.esktransport.feature.passenger.marketplace.presentation

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType

data class MarketplaceUiState(
    val isLoadingVehicleTypes: Boolean = false,
    val isLoadingListings: Boolean = false,
    val vehicleTypes: List<MarketplaceVehicleType> = emptyList(),
    val selectedVehicleTypeCode: String? = null,
    val listings: List<MarketplaceListing> = emptyList(),
    val errorMessage: String? = null,
)
