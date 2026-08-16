package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverIncidentReportApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReportPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverIncidentReportRepository

class DriverIncidentReportRepositoryImpl(
    private val api: DriverIncidentReportApi,
) : DriverIncidentReportRepository {
    override suspend fun getReports(): Result<List<DriverIncidentReport>> {
        return try {
            Result.success(api.getReports().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load incident reports.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun submitReport(payload: DriverIncidentReportPayload): Result<List<DriverIncidentReport>> {
        return try {
            Result.success(api.submitReport(payload.toRequestDto()).data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to submit incident report.")
            Result.failure(IllegalStateException(message))
        }
    }
}
