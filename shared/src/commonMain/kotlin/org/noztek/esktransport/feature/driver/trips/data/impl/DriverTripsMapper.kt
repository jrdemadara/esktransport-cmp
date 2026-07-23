package org.noztek.esktransport.feature.driver.trips.data.impl

import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripFeedbackBundleDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripFeedbackDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripSettlementDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripStopDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripsDashboardDto
import org.noztek.esktransport.feature.driver.trips.data.remote.dto.DriverTripsSummaryDto
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTrip
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripFeedback
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripFeedbackBundle
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripSettlement
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripStatus
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripStop
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsDashboard
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsSummary

fun DriverTripsDashboardDto.toDomain(): DriverTripsDashboard {
    return DriverTripsDashboard(
        currency = currency,
        summary = summary.toDomain(),
        trips = trips.map { it.toDomain() },
    )
}

private fun DriverTripsSummaryDto.toDomain(): DriverTripsSummary {
    return DriverTripsSummary(
        completedTrips = completedTrips,
        onlineSeconds = onlineSeconds,
        grossFare = grossFare,
        platformFee = platformFee,
        netEarning = netEarning,
        from = from,
        to = to,
    )
}

private fun DriverTripDto.toDomain(): DriverTrip {
    return DriverTrip(
        bookingPublicId = bookingPublicId,
        bookingType = bookingType,
        status = status.toTripStatus(),
        passengerName = passengerName,
        vehicleTypeCode = vehicleTypeCode,
        requestedAt = requestedAt,
        assignedAt = assignedAt,
        acceptedAt = acceptedAt,
        pickupConfirmedAt = pickupConfirmedAt,
        completedAt = completedAt,
        canceledAt = canceledAt,
        cancelReason = cancelReason,
        currency = currency,
        finalFare = finalFare,
        paymentMethod = paymentMethod,
        distanceKm = distanceKm,
        durationMin = durationMin,
        pickup = pickup.toDomain(),
        dropoff = dropoff.toDomain(),
        settlement = settlement?.toDomain(),
        feedback = feedback.toDomain(),
    )
}

private fun DriverTripStopDto.toDomain(): DriverTripStop {
    return DriverTripStop(
        label = label,
        lat = lat,
        lng = lng,
    )
}

private fun DriverTripSettlementDto.toDomain(): DriverTripSettlement {
    return DriverTripSettlement(
        publicId = publicId,
        grossFare = grossFare,
        platformFee = platformFee,
        netEarning = netEarning,
        platformFeePercentage = platformFeePercentage,
        settledAt = settledAt,
    )
}

private fun DriverTripFeedbackBundleDto?.toDomain(): DriverTripFeedbackBundle {
    return DriverTripFeedbackBundle(
        passengerToDriver = this?.passengerToDriver?.toDomain(),
        driverToPassenger = this?.driverToPassenger?.toDomain(),
    )
}

private fun DriverTripFeedbackDto.toDomain(): DriverTripFeedback {
    return DriverTripFeedback(
        rating = rating,
        comment = comment,
        submittedAt = submittedAt,
    )
}

private fun String.toTripStatus(): DriverTripStatus {
    return when (lowercase()) {
        "offered" -> DriverTripStatus.Offered
        "accepted" -> DriverTripStatus.Accepted
        "arriving_pickup" -> DriverTripStatus.ArrivingPickup
        "in_progress" -> DriverTripStatus.InProgress
        "completed" -> DriverTripStatus.Completed
        "canceled", "cancelled" -> DriverTripStatus.Cancelled
        "expired" -> DriverTripStatus.Expired
        else -> DriverTripStatus.Unknown
    }
}
