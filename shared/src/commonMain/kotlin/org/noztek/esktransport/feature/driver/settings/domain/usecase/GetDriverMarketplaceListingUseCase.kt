package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverMarketplaceListingRepository

class GetDriverMarketplaceListingUseCase(
    private val repository: DriverMarketplaceListingRepository,
) {
    suspend operator fun invoke(
        vehiclePublicId: String,
        serviceType: DriverVehicleServiceType,
    ): Result<DriverMarketplaceListing> {
        return repository.getListingForVehicle(vehiclePublicId, serviceType)
    }
}
