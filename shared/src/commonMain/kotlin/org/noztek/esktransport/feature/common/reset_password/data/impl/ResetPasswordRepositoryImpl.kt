package org.noztek.esktransport.feature.common.reset_password.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.reset_password.data.remote.ResetPasswordApi
import org.noztek.esktransport.feature.common.reset_password.data.remote.dto.ResetPasswordRequestDto
import org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload
import org.noztek.esktransport.feature.common.reset_password.domain.repository.ResetPasswordRepository

class ResetPasswordRepositoryImpl(
    private val resetPasswordApi: ResetPasswordApi
) : ResetPasswordRepository {
    override suspend fun resetPassword(payload: ResetPasswordPayload): Result<Unit> {
        return try {
            resetPasswordApi.resetPassword(
                ResetPasswordRequestDto(
                    phone = payload.phone,
                    resetToken = payload.resetToken,
                    password = payload.password,
                    passwordConfirmation = payload.passwordConfirmation
                )
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(
                throwable = throwable,
                fallback = "Password reset failed."
            )
            Result.failure(IllegalStateException(message))
        }
    }
}
