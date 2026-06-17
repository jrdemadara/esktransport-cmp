package org.noztek.esktransport.feature.driver.go.presentation

data class GoBookingOfferUiModel(
    val bookingPublicId: String,
    val passengerName: String,
    val pickupLabel: String,
    val destinationLabel: String,
    val fareLabel: String,
)

