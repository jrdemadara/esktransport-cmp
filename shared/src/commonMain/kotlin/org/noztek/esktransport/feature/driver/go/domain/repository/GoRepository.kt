package org.noztek.esktransport.feature.driver.go.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

interface GoRepository {
    suspend fun getAvailability(): Result<Boolean>
    suspend fun setAvailability(isAvailable: Boolean): Result<Boolean>
    suspend fun updateLocation(point: GeoPoint): Result<Unit>
}
