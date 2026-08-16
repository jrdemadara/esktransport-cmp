package org.noztek.esktransport.feature.driver.settings.domain.model

data class DriverEmergencyContact(
    val id: Long,
    val name: String,
    val phone: String,
    val relationship: String,
)

data class DriverEmergencyContactPayload(
    val id: Long? = null,
    val name: String,
    val phone: String,
    val relationship: String,
)
