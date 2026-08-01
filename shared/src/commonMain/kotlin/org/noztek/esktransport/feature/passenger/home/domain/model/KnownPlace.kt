package org.noztek.esktransport.feature.passenger.home.domain.model

data class KnownPlace(
    val id: Long,
    val name: String,
    val category: String,
    val city: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
)
