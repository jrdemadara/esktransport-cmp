package org.noztek.esktransport.feature.common.login.domain.repository

import org.noztek.esktransport.feature.common.login.domain.model.LoginPayload

interface LoginRepository {
    suspend fun login(payload: LoginPayload): Result<Unit>
}
