package org.noztek.esktransport.feature.common.reset_password.domain.repository

import org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload

interface ResetPasswordRepository {
    suspend fun resetPassword(payload: org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload): Result<Unit>
}
