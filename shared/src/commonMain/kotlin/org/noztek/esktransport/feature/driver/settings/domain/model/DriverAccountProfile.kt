package org.noztek.esktransport.feature.driver.settings.domain.model

data class DriverAccountProfile(
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val driverId: Long?,
)
