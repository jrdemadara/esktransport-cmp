package org.noztek.esktransport.feature.passenger.kudi.domain.usecase

import org.noztek.esktransport.feature.passenger.kudi.domain.repository.KudiRepository

class SendKudiMessageUseCase(
    private val repository: KudiRepository,
) {
    suspend operator fun invoke(sessionPublicId: String, message: String) = repository.sendMessage(
        sessionPublicId = sessionPublicId,
        message = message,
    )
}
