package org.noztek.esktransport.feature.driver.settings.domain.repository

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReportPayload

interface DriverIncidentReportRepository {
    suspend fun getReports(): Result<List<DriverIncidentReport>>
    suspend fun submitReport(payload: DriverIncidentReportPayload): Result<List<DriverIncidentReport>>
}
