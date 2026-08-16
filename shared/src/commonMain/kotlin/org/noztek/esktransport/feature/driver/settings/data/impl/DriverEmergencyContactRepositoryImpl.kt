package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverEmergencyContactApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContactPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverEmergencyContactRepository

class DriverEmergencyContactRepositoryImpl(
    private val api: DriverEmergencyContactApi,
) : DriverEmergencyContactRepository {
    override suspend fun getContacts(): Result<List<DriverEmergencyContact>> {
        return try {
            Result.success(api.getContacts().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load emergency contacts.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun saveContact(payload: DriverEmergencyContactPayload): Result<List<DriverEmergencyContact>> {
        return try {
            val response = if (payload.id == null) {
                api.createContact(payload.toRequestDto())
            } else {
                api.updateContact(
                    contactId = payload.id,
                    request = payload.toRequestDto(),
                )
            }

            Result.success(response.data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to save emergency contact.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun deleteContact(contactId: Long): Result<List<DriverEmergencyContact>> {
        return try {
            Result.success(
                api.deleteContact(contactId).data.map { it.toDomain() },
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to delete emergency contact.")
            Result.failure(IllegalStateException(message))
        }
    }
}
