package org.noztek.esktransport.feature.driver.home.presentation

data class DriverHomeBookingOfferUiModel(
    val bookingPublicId: String,
    val passengerName: String,
    val pickupLabel: String,
    val destinationLabel: String,
    val fareLabel: String,
)

