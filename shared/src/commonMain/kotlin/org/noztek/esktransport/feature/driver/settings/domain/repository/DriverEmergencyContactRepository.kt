package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContactPayload

interface DriverEmergencyContactRepository {
    suspend fun getContacts(): Result<List<DriverEmergencyContact>>
    suspend fun saveContact(payload: DriverEmergencyContactPayload): Result<List<DriverEmergencyContact>>
    suspend fun deleteContact(contactId: Long): Result<List<DriverEmergencyContact>>
}
