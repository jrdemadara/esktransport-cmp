package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverEmergencyContactRepository

class DeleteDriverEmergencyContactUseCase(
    private val repository: DriverEmergencyContactRepository,
) {
    suspend operator fun invoke(contactId: Long): Result<List<DriverEmergencyContact>> {
        return repository.deleteContact(contactId)
    }
}
