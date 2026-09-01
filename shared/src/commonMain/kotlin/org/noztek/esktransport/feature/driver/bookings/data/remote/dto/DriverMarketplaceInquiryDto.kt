package org.noztek.esktransport.feature.driver.bookings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiryCustomer
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiryVehicle

@Serializable
data class DriverMarketplaceInquiriesResponseDto(
    val data: List<DriverMarketplaceInquiryDto> = emptyList(),
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
