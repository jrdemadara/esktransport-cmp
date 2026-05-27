package org.noztek.esktransport.feature.common.register.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.register.data.remote.RegisterApi
import org.noztek.esktransport.feature.common.register.data.remote.dto.RegisterRequestDto
import org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload
import org.noztek.esktransport.feature.common.register.domain.repository.RegisterRepository

class RegisterRepositoryImpl(
    private val registerApi: RegisterApi,
) : RegisterRepository {
    override suspend fun register(payload: RegisterPayload): Result<Unit> {
        return try {
            registerApi.register(
                RegisterRequestDto(
                    name = payload.name,
                    phone = payload.phone,
                    email = payload.email,
                    password = payload.password,
                    passwordConfirmation = payload.passwordConfirmation,
                    role = payload.role.apiValue,
                ),
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Registration failed.")
            Result.failure(IllegalStateException(message))
        }
    }
}
