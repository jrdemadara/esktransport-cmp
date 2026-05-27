package org.noztek.esktransport.feature.common.logout.domain.usecase

import org.noztek.esktransport.feature.common.logout.domain.repository.LogoutRepository

class LogoutUseCase(private val repository: LogoutRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
