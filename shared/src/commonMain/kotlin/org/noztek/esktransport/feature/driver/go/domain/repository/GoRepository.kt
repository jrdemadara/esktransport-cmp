package org.noztek.esktransport.feature.driver.go.domain.repository

interface GoRepository {
    suspend fun getAvailability(): Result<Boolean>
    suspend fun setAvailability(isAvailable: Boolean): Result<Boolean>
}
