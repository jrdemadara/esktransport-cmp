package org.noztek.esktransport.feature.passenger.marketplace.presentation

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing

data class MarketplaceListingDetailsUiState(
    val isLoading: Boolean = false,
    val listing: MarketplaceListing? = null,
    val errorMessage: String? = null,
)
