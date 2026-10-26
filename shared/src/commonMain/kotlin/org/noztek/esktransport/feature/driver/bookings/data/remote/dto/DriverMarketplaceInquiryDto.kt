package org.noztek.esktransport.feature.driver.bookings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInbox
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiryCustomer
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiryVehicle
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceRequest

@Serializable
data class DriverMarketplaceInquiriesResponseDto(
    val bookings: List<DriverMarketplaceInquiryDto> = emptyList(),
    val inquiries: List<DriverMarketplaceInquiryDto> = emptyList(),
    @SerialName("badge_count") val badgeCount: Int = 0,
)

@Serializable
data class ReviewMarketplaceRequestDto(
    @SerialName("quoted_amount") val quotedAmount: Double,
    val currency: String = "PHP",
    @SerialName("owner_note") val ownerNote: String? = null,
)

@Serializable
data class DriverMarketplaceRequestResponseDto(
    val data: DriverMarketplaceRequestDto,
)

@Serializable
data class DriverMarketplaceRequestDto(
    @SerialName("public_id") val publicId: String,
    @SerialName("service_type") val serviceType: String,
    val status: String,
    @SerialName("workflow_status") val workflowStatus: String,
    @SerialName("listing_title") val listingTitle: String = "",
    val customer: DriverMarketplaceInquiryCustomerDto,
    val vehicle: DriverMarketplaceInquiryVehicleDto,
    @SerialName("requested_start_at") val requestedStartAt: String? = null,
    @SerialName("requested_end_at") val requestedEndAt: String? = null,
    @SerialName("pickup_label") val pickupLabel: String? = null,
    @SerialName("destination_label") val destinationLabel: String? = null,
    @SerialName("passenger_count") val passengerCount: Int? = null,
    @SerialName("cargo_description") val cargoDescription: String? = null,
    @SerialName("cargo_weight_kg") val cargoWeightKg: Double? = null,
    @SerialName("cargo_pieces") val cargoPieces: Int? = null,
    @SerialName("customer_note") val customerNote: String? = null,
    @SerialName("owner_note") val ownerNote: String? = null,
    @SerialName("quoted_amount") val quotedAmount: Double? = null,
    @SerialName("final_amount") val finalAmount: Double? = null,
    val currency: String = "PHP",
    @SerialName("thread_id") val threadId: Long? = null,
)

@Serializable
data class DriverMarketplaceInquiryDto(
    val id: Long,
    @SerialName("thread_type")
    val threadType: String,
    @SerialName("service_type")
    val serviceType: String? = null,
    @SerialName("vehicle_service_request_id")
    val vehicleServiceRequestId: Long? = null,
    @SerialName("vehicle_service_request_public_id")
    val vehicleServiceRequestPublicId: String? = null,
    @SerialName("vehicle_service_request_status")
    val vehicleServiceRequestStatus: String? = null,
    @SerialName("vehicle_service_request_workflow_status")
    val vehicleServiceRequestWorkflowStatus: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("has_unread") val hasUnread: Boolean = false,
    val customer: DriverMarketplaceInquiryCustomerDto,
    val vehicle: DriverMarketplaceInquiryVehicleDto,
    @SerialName("latest_message")
    val latestMessage: String? = null,
    @SerialName("latest_sent_at")
    val latestSentAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
)

@Serializable
data class DriverMarketplaceInquiryCustomerDto(
    val name: String,
    val phone: String? = null,
)

@Serializable
data class DriverMarketplaceInquiryVehicleDto(
    val plate: String? = null,
    val make: String? = null,
    val model: String? = null,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
)

fun DriverMarketplaceInquiryDto.toDomain(): DriverMarketplaceInquiry {
    return DriverMarketplaceInquiry(
        id = id,
        threadType = threadType,
        serviceType = serviceType,
        vehicleServiceRequestId = vehicleServiceRequestId,
        vehicleServiceRequestPublicId = vehicleServiceRequestPublicId,
        vehicleServiceRequestStatus = vehicleServiceRequestStatus,
        vehicleServiceRequestWorkflowStatus = vehicleServiceRequestWorkflowStatus,
        unreadCount = unreadCount,
        hasUnread = hasUnread,
        customer = DriverMarketplaceInquiryCustomer(
            name = customer.name,
            phone = customer.phone,
        ),
        vehicle = DriverMarketplaceInquiryVehicle(
            plate = vehicle.plate,
            make = vehicle.make,
            model = vehicle.model,
            vehicleTypeCode = vehicle.vehicleTypeCode,
        ),
        latestMessage = latestMessage,
        latestSentAt = latestSentAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun DriverMarketplaceRequestDto.toDomain(): DriverMarketplaceRequest {
    return DriverMarketplaceRequest(
        publicId = publicId,
        serviceType = serviceType,
        status = status,
        workflowStatus = workflowStatus,
        listingTitle = listingTitle,
        customerName = customer.name,
        customerPhone = customer.phone,
        vehicle = DriverMarketplaceInquiryVehicle(
            plate = vehicle.plate,
            make = vehicle.make,
            model = vehicle.model,
            vehicleTypeCode = vehicle.vehicleTypeCode,
        ),
        requestedStartAt = requestedStartAt,
        requestedEndAt = requestedEndAt,
        pickupLabel = pickupLabel,
        destinationLabel = destinationLabel,
        passengerCount = passengerCount,
        cargoDescription = cargoDescription,
        cargoWeightKg = cargoWeightKg,
        cargoPieces = cargoPieces,
        customerNote = customerNote,
        ownerNote = ownerNote,
        quotedAmount = quotedAmount,
        finalAmount = finalAmount,
        currency = currency,
        threadId = threadId,
    )
}

fun DriverMarketplaceInquiriesResponseDto.toDomain(): DriverMarketplaceInbox {
    return DriverMarketplaceInbox(
        bookings = bookings.map { it.toDomain() },
        inquiries = inquiries.map { it.toDomain() },
        badgeCount = badgeCount,
    )
}
