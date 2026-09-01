package org.noztek.esktransport.feature.driver.bookings.domain.model

data class DriverMarketplaceInquiry(
    val id: Long,
    val threadType: String,
    val serviceType: String?,
    val vehicleServiceRequestId: Long?,
    val customer: DriverMarketplaceInquiryCustomer,
    val vehicle: DriverMarketplaceInquiryVehicle,
    val latestMessage: String?,
    val latestSentAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class DriverMarketplaceInquiryCustomer(
    val name: String,
    val phone: String?,
)

data class DriverMarketplaceInquiryVehicle(
    val plate: String?,
    val make: String?,
    val model: String?,
    val vehicleTypeCode: String?,
)
