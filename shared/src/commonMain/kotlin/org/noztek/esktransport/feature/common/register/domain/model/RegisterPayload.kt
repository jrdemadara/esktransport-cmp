package org.noztek.esktransport.feature.common.register.domain.model

data class RegisterPayload(
    val name: String,
    val phone: String,
    val email: String?,
    val password: String,
    val passwordConfirmation: String,
    val role: org.noztek.esktransport.feature.common.register.domain.model.RegisterRole,
)
