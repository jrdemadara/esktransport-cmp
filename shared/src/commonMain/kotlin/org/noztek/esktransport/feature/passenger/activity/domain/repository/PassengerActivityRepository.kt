package org.noztek.esktransport.feature.passenger.activity.domain.repository

import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerActivityDashboard

interface PassengerActivityRepository {
    suspend fun getActivity(): Result<PassengerActivityDashboard>
}
