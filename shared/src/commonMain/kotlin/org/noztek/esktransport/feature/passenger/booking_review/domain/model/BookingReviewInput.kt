package org.noztek.esktransport.feature.passenger.booking_review.domain.model

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

data class BookingReviewInput(
    val pickupLocation: String,
    val destinationLocation: String,
    val pickupPoint: GeoPoint,
    val destinationPoint: GeoPoint,
    val passengerCount: Int,
    val vehicleTypeIndex: Int,
    val routePoints: List<GeoPoint> = emptyList(),
    val notes: String? = null
) {
    val vehicleTypeCode: String
        get() = when (vehicleTypeIndex) {
            0 -> "motorcycle"
            1 -> "tricycle"
            2 -> "car"
            3 -> "van"
            else -> "motorcycle"
        }
    val requiredSeats: Int
        get() = passengerCount + 1
}
