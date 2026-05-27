package org.noztek.esktransport.core.realtime.model

data class PassengerDriverAssignedEvent(
    val bookingPublicId: String,
    val riderUserId: Long,
    val driverPublicId: String?,
    val vehicleTypeCode: String?,
    val vehicleLabel: String?,
    val vehiclePlate: String?,
    val passengerCapacity: Int?,
    val finalFare: Double?,
)
