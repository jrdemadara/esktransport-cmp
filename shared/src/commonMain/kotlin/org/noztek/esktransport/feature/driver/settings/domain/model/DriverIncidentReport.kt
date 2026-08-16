package org.noztek.esktransport.feature.driver.settings.domain.model

data class DriverIncidentReport(
    val id: Long,
    val category: DriverIncidentCategory,
    val urgency: DriverIncidentUrgency,
    val bookingReference: String?,
    val details: String,
    val status: String,
    val createdAt: String,
)

data class DriverIncidentReportPayload(
    val category: DriverIncidentCategory,
    val urgency: DriverIncidentUrgency,
    val bookingReference: String?,
    val details: String,
)

enum class DriverIncidentCategory(
    val code: String,
    val label: String,
) {
    Trip("trip", "Trip"),
    Passenger("passenger", "Passenger"),
    Payment("payment", "Payment"),
    Vehicle("vehicle", "Vehicle"),
    Safety("safety", "Safety"),
    Other("other", "Other"),
}

enum class DriverIncidentUrgency(
    val code: String,
    val label: String,
) {
    Normal("normal", "Normal"),
    Urgent("urgent", "Urgent"),
}

fun String.toDriverIncidentCategory(): DriverIncidentCategory {
    return DriverIncidentCategory.entries.firstOrNull { it.code == this } ?: DriverIncidentCategory.Other
}

fun String.toDriverIncidentUrgency(): DriverIncidentUrgency {
    return DriverIncidentUrgency.entries.firstOrNull { it.code == this } ?: DriverIncidentUrgency.Normal
}
