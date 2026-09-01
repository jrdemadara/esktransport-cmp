package org.noztek.esktransport.feature.driver.bookings.presentation

import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry

data class DriverBookingsUiState(
    val inquiries: List<DriverMarketplaceInquiry> = emptyList(),
    val selectedFilter: DriverBookingFilter = DriverBookingFilter.All,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val visibleInquiries: List<DriverMarketplaceInquiry>
        get() = when (selectedFilter) {
            DriverBookingFilter.All -> inquiries
            DriverBookingFilter.Rental -> inquiries.filter { it.serviceType == "rental" }
            DriverBookingFilter.Cargo -> inquiries.filter { it.serviceType == "cargo" }
        }
}

enum class DriverBookingFilter(
    val label: String,
) {
    All("All"),
    Rental("Rental"),
    Cargo("Cargo"),
}
