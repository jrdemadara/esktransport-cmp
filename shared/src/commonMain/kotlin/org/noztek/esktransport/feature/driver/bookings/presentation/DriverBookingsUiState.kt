package org.noztek.esktransport.feature.driver.bookings.presentation

import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry

data class DriverBookingsUiState(
    val bookings: List<DriverMarketplaceInquiry> = emptyList(),
    val inquiries: List<DriverMarketplaceInquiry> = emptyList(),
    val badgeCount: Int = 0,
    val selectedFilter: DriverBookingFilter = DriverBookingFilter.All,
    val isLoading: Boolean = false,
    val acceptingRequestPublicId: String? = null,
    val errorMessage: String? = null,
) {
    val visibleBookings: List<DriverMarketplaceInquiry>
        get() = when (selectedFilter) {
            DriverBookingFilter.All -> bookings
            DriverBookingFilter.Rental -> bookings.filter { it.serviceType == "rental" }
            DriverBookingFilter.Cargo -> bookings.filter { it.serviceType == "cargo" }
        }
}

enum class DriverBookingFilter(
    val label: String,
) {
    All("All"),
    Rental("Rental"),
    Cargo("Cargo"),
}
