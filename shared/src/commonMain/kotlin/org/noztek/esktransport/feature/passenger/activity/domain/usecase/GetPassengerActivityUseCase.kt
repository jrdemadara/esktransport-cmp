package org.noztek.esktransport.feature.passenger.activity.domain.usecase

import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerActivityDashboard
import org.noztek.esktransport.feature.passenger.activity.domain.repository.PassengerActivityRepository

class GetPassengerActivityUseCase(
    private val repository: PassengerActivityRepository,
) {
    suspend operator fun invoke(): Result<PassengerActivityDashboard> = repository.getActivity()
}
