package org.noztek.esktransport.feature.passenger.kudi.domain.usecase

import org.noztek.esktransport.feature.passenger.kudi.domain.repository.KudiRepository

class GetCurrentKudiSessionUseCase(
    private val repository: KudiRepository,
) {
    suspend operator fun invoke() = repository.getCurrentSession()
}
