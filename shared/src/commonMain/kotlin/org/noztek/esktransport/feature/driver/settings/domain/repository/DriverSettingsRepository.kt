package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile

interface DriverSettingsRepository {
    suspend fun getAccount(): Result<DriverAccountProfile>
    suspend fun updateAccount(email: String?, address: String?): Result<DriverAccountProfile>
    suspend fun getProfilePhoto(): Result<ByteArray?>
}
