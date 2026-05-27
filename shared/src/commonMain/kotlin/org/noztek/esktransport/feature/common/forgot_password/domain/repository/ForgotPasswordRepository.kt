package org.noztek.esktransport.feature.common.forgot_password.domain.repository

import org.noztek.esktransport.feature.common.forgot_password.domain.model.ForgotPasswordPayload

interface ForgotPasswordRepository {
    suspend fun forgotPassword(payload: ForgotPasswordPayload): Result<Unit>
}