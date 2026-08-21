package org.noztek.esktransport.feature.passenger.marketplace.data.impl

import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.MarketplaceListingDto
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.MarketplaceVehicleTypeDto
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListingOwner
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListingVehicle
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType

fun MarketplaceVehicleTypeDto.toDomain(): MarketplaceVehicleType {
    return MarketplaceVehicleType(
        code = code,
        name = name,
        description = description,
        supportsCargo = supportsCargo,
        passengerMax = passengerMax,
        sortOrder = sortOrder,
        allowedServices = allowedServices,
    )
}

fun MarketplaceListingDto.toDomain(): MarketplaceListing {
    return MarketplaceListing(
        publicId = publicId,
        serviceType = serviceType,
        title = title,
        description = description,
        baseRate = baseRate,
        rateUnit = rateUnit,
        minimumHours = minimumHours,
        includedKm = includedKm,
        currency = currency,
        owner = MarketplaceListingOwner(name = owner.name),
        vehicle = MarketplaceListingVehicle(
            id = vehicle.id,
            vehicleTypeCode = vehicle.vehicleTypeCode,
            vehicleTypeLabel = vehicle.vehicleTypeLabel,
            plate = vehicle.plate,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            payloadKg = vehicle.payloadKg,
            volumeM3 = vehicle.volumeM3,
            passengerCapacity = vehicle.passengerCapacity,
        ),
        updatedAt = updatedAt,
    )
}
