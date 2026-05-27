package org.noztek.esktransport.feature.common.login.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val phone: String,
    val password: String,
    @SerialName("device_name")
    val deviceName: String?
)

@Serializable
data class LoginResponseDto(
    @SerialName(value = "token_type")
    val tokenType: String,
    @SerialName(value = "access_token")
    val accessToken: String,
    @SerialName(value = "expires_at")
    val expiresAt: String?,
    val abilities: List<String>,
    val data: LoginUserDto
)

@Serializable
data class LoginUserDto(
    val id: Int,
    @SerialName(value = "public_id")
    val publicId: String,
    val name: String,
    val phone: String,
    val email: String?,
    val status: String,
    val roles: List<String>,
    @SerialName(value = "created_at")
    val createdAt: String,
    @SerialName(value = "updated_at")
    val updatedAt: String
)
