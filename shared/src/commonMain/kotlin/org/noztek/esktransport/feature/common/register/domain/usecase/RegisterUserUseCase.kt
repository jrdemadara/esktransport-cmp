package org.noztek.esktransport.feature.common.register.domain.usecase

import org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload
import org.noztek.esktransport.feature.common.register.domain.repository.RegisterRepository

class RegisterUserUseCase(
    private val repository: org.noztek.esktransport.feature.common.register.domain.repository.RegisterRepository,
) {
    suspend operator fun invoke(payload: org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload): Result<Unit> = repository.register(payload)
}
