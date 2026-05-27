package org.noztek.esktransport.feature.common.reset_password.domain.usecase

import org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload
import org.noztek.esktransport.feature.common.reset_password.domain.repository.ResetPasswordRepository

class ResetPasswordUseCase(
    private val repository: org.noztek.esktransport.feature.common.reset_password.domain.repository.ResetPasswordRepository
) {
    suspend operator fun invoke(payload: org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload): Result<Unit> {
        return repository.resetPassword(payload)
    }
}
