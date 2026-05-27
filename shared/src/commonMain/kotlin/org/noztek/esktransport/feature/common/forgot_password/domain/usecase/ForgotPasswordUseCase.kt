package org.noztek.esktransport.feature.common.forgot_password.domain.usecase

import org.noztek.esktransport.feature.common.forgot_password.domain.model.ForgotPasswordPayload
import org.noztek.esktransport.feature.common.forgot_password.domain.repository.ForgotPasswordRepository

class ForgotPasswordUseCase(
    private val repository: ForgotPasswordRepository
) {
    suspend operator fun invoke(payload: ForgotPasswordPayload): Result<Unit> {
        return repository.forgotPassword(payload)
    }
}
