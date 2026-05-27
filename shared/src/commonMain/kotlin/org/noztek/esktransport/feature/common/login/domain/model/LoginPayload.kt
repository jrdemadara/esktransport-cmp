package org.noztek.esktransport.feature.common.login.domain.model

data class LoginPayload(
    val phone: String,
    val password: String,
    val deviceName: String
)