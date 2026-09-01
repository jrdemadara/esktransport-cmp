package org.noztek.esktransport.feature.passenger.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerAccountProfile

@Serializable
data class PassengerAccountResponseDto(
    val data: PassengerAccountDto,
)

@Serializable
data class PassengerAccountDto(
    val id: Long? = null,
    @SerialName("public_id")
    val publicId: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("has_profile_photo")
    val hasProfilePhoto: Boolean = false,
    val status: String? = null,
)

@Serializable
data class UpdatePassengerAccountRequestDto(
    val email: String? = null,
    val address: String? = null,
)

fun PassengerAccountDto.toDomain(): PassengerAccountProfile {
    return PassengerAccountProfile(
        id = id,
        publicId = publicId,
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        email = email.orEmpty(),
        address = address.orEmpty(),
        hasProfilePhoto = hasProfilePhoto,
        status = status.orEmpty(),
    )
}
