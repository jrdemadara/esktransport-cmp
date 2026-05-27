package org.noztek.esktransport.feature.common.login.domain.usecase

import org.noztek.esktransport.feature.common.login.domain.model.LoginPayload
import org.noztek.esktransport.feature.common.login.domain.repository.LoginRepository

class LoginUseCase(private val repository: LoginRepository) {
    suspend operator fun invoke(payload: LoginPayload): Result<Unit> = repository.login(payload)
}