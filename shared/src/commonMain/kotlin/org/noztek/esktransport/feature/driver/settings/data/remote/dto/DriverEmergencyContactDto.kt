package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContactPayload

@Serializable
data class DriverEmergencyContactsResponseDto(
    val data: List<DriverEmergencyContactDto> = emptyList(),
)

@Serializable
data class DriverEmergencyContactDto(
    val id: Long,
    val name: String,
    val phone: String,
    val relationship: String,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
)

@Serializable
data class DriverEmergencyContactRequestDto(
    val name: String,
    val phone: String,
    val relationship: String,
)

fun DriverEmergencyContactDto.toDomain(): DriverEmergencyContact {
    return DriverEmergencyContact(
        id = id,
        name = name,
        phone = phone,
        relationship = relationship,
    )
}

fun DriverEmergencyContactPayload.toRequestDto(): DriverEmergencyContactRequestDto {
    return DriverEmergencyContactRequestDto(
        name = name.trim(),
        phone = phone.trim(),
        relationship = relationship.trim(),
    )
}
