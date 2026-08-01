package org.noztek.esktransport.feature.passenger.activity.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.activity.data.remote.PassengerActivityApi
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerActivityDashboard
import org.noztek.esktransport.feature.passenger.activity.domain.repository.PassengerActivityRepository

class PassengerActivityRepositoryImpl(
    private val api: PassengerActivityApi,
) : PassengerActivityRepository {
    override suspend fun getActivity(): Result<PassengerActivityDashboard> {
        return try {
            Result.success(api.getActivity().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load activity.")
            Result.failure(IllegalStateException(message))
        }
    }
}
