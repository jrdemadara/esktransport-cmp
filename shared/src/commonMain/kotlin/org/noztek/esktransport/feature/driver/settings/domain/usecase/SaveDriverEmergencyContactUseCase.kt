package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContactPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverEmergencyContactRepository

class SaveDriverEmergencyContactUseCase(
    private val repository: DriverEmergencyContactRepository,
) {
    suspend operator fun invoke(payload: DriverEmergencyContactPayload): Result<List<DriverEmergencyContact>> {
        return repository.saveContact(payload)
    }
}
