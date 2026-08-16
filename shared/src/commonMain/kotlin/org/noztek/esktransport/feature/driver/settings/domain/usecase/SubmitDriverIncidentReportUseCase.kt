package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReportPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverIncidentReportRepository

class SubmitDriverIncidentReportUseCase(
    private val repository: DriverIncidentReportRepository,
) {
    suspend operator fun invoke(payload: DriverIncidentReportPayload): Result<List<DriverIncidentReport>> {
        return repository.submitReport(payload)
    }
}
