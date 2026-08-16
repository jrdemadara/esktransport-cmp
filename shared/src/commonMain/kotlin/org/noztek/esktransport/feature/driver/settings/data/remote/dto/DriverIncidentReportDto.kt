package org.noztek.esktransport.feature.driver.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReportPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.toDriverIncidentCategory
import org.noztek.esktransport.feature.driver.settings.domain.model.toDriverIncidentUrgency

@Serializable
data class DriverIncidentReportsResponseDto(
    val data: List<DriverIncidentReportDto> = emptyList(),
)

@Serializable
data class DriverIncidentReportDto(
    val id: Long,
    val category: String,
    val urgency: String,
    @SerialName("booking_reference")
    val bookingReference: String? = null,
    val details: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
)

@Serializable
data class DriverIncidentReportRequestDto(
    val category: String,
    val urgency: String,
    @SerialName("booking_reference")
    val bookingReference: String? = null,
    val details: String,
)

fun DriverIncidentReportDto.toDomain(): DriverIncidentReport {
    return DriverIncidentReport(
        id = id,
        category = category.toDriverIncidentCategory(),
        urgency = urgency.toDriverIncidentUrgency(),
        bookingReference = bookingReference,
        details = details,
        status = status,
        createdAt = createdAt,
    )
}

fun DriverIncidentReportPayload.toRequestDto(): DriverIncidentReportRequestDto {
    return DriverIncidentReportRequestDto(
        category = category.code,
        urgency = urgency.code,
        bookingReference = bookingReference?.trim()?.takeIf { it.isNotBlank() },
        details = details.trim(),
    )
}
