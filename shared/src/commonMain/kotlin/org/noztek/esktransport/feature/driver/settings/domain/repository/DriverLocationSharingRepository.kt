package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettingsPayload

interface DriverLocationSharingRepository {
    suspend fun getSettings(): Result<DriverLocationSharingSettings>
    suspend fun updateSettings(payload: DriverLocationSharingSettingsPayload): Result<DriverLocationSharingSettings>
}
