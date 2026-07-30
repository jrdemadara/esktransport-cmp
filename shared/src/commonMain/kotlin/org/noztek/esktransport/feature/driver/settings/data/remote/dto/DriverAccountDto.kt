package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile

@Serializable
data class DriverAccountResponseDto(
    val data: DriverAccountDto,
)

@Serializable
data class DriverAccountDto(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("driver_id")
    val driverId: Long? = null,
)

@Serializable
data class UpdateDriverAccountRequestDto(
    val email: String? = null,
    val address: String? = null,
)

fun DriverAccountDto.toDomain(): DriverAccountProfile {
    return DriverAccountProfile(
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        email = email.orEmpty(),
        address = address.orEmpty(),
        driverId = driverId,
    )
}
