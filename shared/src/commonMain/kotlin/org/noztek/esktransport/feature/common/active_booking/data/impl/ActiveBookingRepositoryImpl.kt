package org.noztek.esktransport.feature.common.active_booking.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.active_booking.data.remote.ActiveBookingApi
import org.noztek.esktransport.feature.common.active_booking.data.remote.dto.ActiveBookingDataDto
import org.noztek.esktransport.feature.common.active_booking.data.remote.dto.ActiveBookingDriverDto
import org.noztek.esktransport.feature.common.active_booking.data.remote.dto.ActiveBookingPassengerDto
import org.noztek.esktransport.feature.common.active_booking.data.remote.dto.ActiveBookingPointDto
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBooking
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingDriver
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingPassenger
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingPoint
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingStatus
import org.noztek.esktransport.feature.common.active_booking.domain.repository.ActiveBookingRepository

class ActiveBookingRepositoryImpl(
    private val api: ActiveBookingApi,
) : ActiveBookingRepository {
    override suspend fun getPassengerActiveBooking(): Result<ActiveBooking?> {
        return runCatching { api.getPassengerActiveBooking().data?.toDomain() }
            .recoverCatching { throw IllegalStateException(ApiErrorParser.parse(it, "Failed to load active booking.")) }
    }

    override suspend fun getDriverActiveBooking(): Result<ActiveBooking?> {
        return runCatching { api.getDriverActiveBooking().data?.toDomain() }
            .recoverCatching { throw IllegalStateException(ApiErrorParser.parse(it, "Failed to load active booking.")) }
    }
}

private fun ActiveBookingDataDto.toDomain(): ActiveBooking {
    return ActiveBooking(
        bookingPublicId = bookingPublicId,
        status = status.toActiveBookingStatus(),
        finalFare = finalFare,
        currency = currency,
        requestedAt = requestedAt,
        searchExpiresAt = searchExpiresAt,
        offerExpiresAt = offerExpiresAt,
        pickup = pickup?.toDomain(),
        destination = destination?.toDomain(),
        driver = driver?.toDomain(),
        passenger = passenger?.toDomain(),
    )
}

private fun String.toActiveBookingStatus(): ActiveBookingStatus = when (lowercase()) {
    "searching" -> ActiveBookingStatus.SEARCHING
    "offered" -> ActiveBookingStatus.OFFERED
    "accepted" -> ActiveBookingStatus.ACCEPTED
    "arriving_pickup" -> ActiveBookingStatus.ARRIVING_PICKUP
    "in_progress" -> ActiveBookingStatus.IN_PROGRESS
    else -> ActiveBookingStatus.UNKNOWN
}

private fun ActiveBookingPointDto.toDomain() = ActiveBookingPoint(
    label = label,
    latitude = lat,
    longitude = lng,
)

private fun ActiveBookingDriverDto.toDomain() = ActiveBookingDriver(
    riderUserId = riderUserId,
    driverPublicId = driverPublicId,
    name = name,
    vehicleTypeCode = vehicleTypeCode,
    vehicleLabel = vehicleLabel,
    vehiclePlate = vehiclePlate,
    passengerCapacity = passengerCapacity,
)

private fun ActiveBookingPassengerDto.toDomain() = ActiveBookingPassenger(
    userId = userId,
    name = name,
)
