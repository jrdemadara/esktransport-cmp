package org.noztek.esktransport.feature.driver.settings.domain.repository

interface DriverSettingsRepository {
    suspend fun getProfilePhoto(): Result<ByteArray?>
}
