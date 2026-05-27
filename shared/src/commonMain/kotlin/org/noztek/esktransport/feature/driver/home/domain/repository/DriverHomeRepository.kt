package org.noztek.esktransport.feature.driver.home.domain.repository

interface DriverHomeRepository {
    suspend fun getAvailability(): Result<Boolean>
    suspend fun setAvailability(isAvailable: Boolean): Result<Boolean>
}
