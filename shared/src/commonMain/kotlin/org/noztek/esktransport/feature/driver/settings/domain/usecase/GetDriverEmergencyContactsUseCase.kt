package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverEmergencyContactRepository

class GetDriverEmergencyContactsUseCase(
    private val repository: DriverEmergencyContactRepository,
) {
    suspend operator fun invoke(): Result<List<DriverEmergencyContact>> = repository.getContacts()
}
