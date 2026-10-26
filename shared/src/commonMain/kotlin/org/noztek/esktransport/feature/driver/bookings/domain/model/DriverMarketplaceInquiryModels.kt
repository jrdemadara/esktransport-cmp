package org.noztek.esktransport.feature.driver.bookings.domain.model

data class DriverMarketplaceInquiry(
    val id: Long,
    val threadType: String,
    val serviceType: String?,
    val vehicleServiceRequestId: Long?,
    val vehicleServiceRequestPublicId: String?,
    val vehicleServiceRequestStatus: String?,
    val vehicleServiceRequestWorkflowStatus: String?,
    val unreadCount: Int = 0,
    val hasUnread: Boolean = false,
    val customer: DriverMarketplaceInquiryCustomer,
    val vehicle: DriverMarketplaceInquiryVehicle,
    val latestMessage: String?,
    val latestSentAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class DriverMarketplaceInbox(
    val bookings: List<DriverMarketplaceInquiry> = emptyList(),
    val inquiries: List<DriverMarketplaceInquiry> = emptyList(),
    val badgeCount: Int = 0,
)

data class DriverMarketplaceRequest(
    val publicId: String,
    val serviceType: String,
    val status: String,
    val workflowStatus: String,
    val listingTitle: String,
    val customerName: String,
    val customerPhone: String?,
    val vehicle: DriverMarketplaceInquiryVehicle,
    val requestedStartAt: String?,
    val requestedEndAt: String?,
    val pickupLabel: String?,
    val destinationLabel: String?,
    val passengerCount: Int?,
    val cargoDescription: String?,
    val cargoWeightKg: Double?,
    val cargoPieces: Int?,
    val customerNote: String?,
    val ownerNote: String?,
    val quotedAmount: Double?,
    val finalAmount: Double?,
    val currency: String,
    val threadId: Long?,
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
