package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverIncidentReportRepository

class GetDriverIncidentReportsUseCase(
    private val repository: DriverIncidentReportRepository,
) {
    suspend operator fun invoke(): Result<List<DriverIncidentReport>> = repository.getReports()
}
