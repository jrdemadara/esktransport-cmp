package org.noztek.esktransport.feature.common.forgot_password.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.forgot_password.data.remote.ForgotPasswordApi
import org.noztek.esktransport.feature.common.forgot_password.data.remote.dto.ForgotPasswordRequestDto
import org.noztek.esktransport.feature.common.forgot_password.domain.model.ForgotPasswordPayload
import org.noztek.esktransport.feature.common.forgot_password.domain.repository.ForgotPasswordRepository

class ForgotPasswordRepositoryImpl(
    private val forgotPasswordApi: ForgotPasswordApi
) : ForgotPasswordRepository {
    override suspend fun forgotPassword(payload: ForgotPasswordPayload): Result<Unit> {
        return try {
            forgotPasswordApi.forgotPassword(
                ForgotPasswordRequestDto(
                    phone = payload.phone
                )
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to send reset password request.")
            Result.failure(IllegalStateException(message))
        }
    }
}
