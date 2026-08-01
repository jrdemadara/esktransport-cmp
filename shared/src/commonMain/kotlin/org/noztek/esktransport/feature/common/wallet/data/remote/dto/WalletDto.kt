package org.noztek.esktransport.feature.common.wallet.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletResponseDto(
    val data: WalletDashboardDto,
)

@Serializable
data class WalletDashboardDto(
    val wallet: WalletDto,
    @SerialName("driver_mode_requirement")
    val driverModeRequirement: DriverModeWalletRequirementDto = DriverModeWalletRequirementDto(),
    @SerialName("recent_ledger_entries")
    val recentLedgerEntries: List<WalletLedgerEntryDto> = emptyList(),
    @SerialName("pending_topups")
    val pendingTopups: List<WalletTopupDto> = emptyList(),
    @SerialName("pending_cashouts")
    val pendingCashouts: List<WalletCashoutDto> = emptyList(),
)

@Serializable
data class DriverModeWalletRequirementDto(
    val currency: String = "PHP",
    @SerialName("minimum_wallet_balance")
    val minimumWalletBalance: Double = 0.0,
    @SerialName("has_minimum_wallet_balance")
    val hasMinimumWalletBalance: Boolean = true,
)

@Serializable
data class WalletDto(
    val id: Long,
    @SerialName("user_id")
    val userId: Long,
    val balance: Double,
    val currency: String,
    @SerialName("last_ledger_at")
    val lastLedgerAt: String? = null,
)

@Serializable
data class WalletTopupDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("reference_code")
    val referenceCode: String,
    @SerialName("qr_payload")
    val qrPayload: String,
    val amount: Double,
    val currency: String,
    val status: String,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class WalletCashoutDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("reference_code")
    val referenceCode: String,
    @SerialName("qr_payload")
    val qrPayload: String,
    val amount: Double,
    val currency: String,
    val status: String,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)

@Serializable
data class WalletLedgerEntryDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("entry_type")
    val entryType: String,
    val direction: String,
    val amount: Double,
    val currency: String,
    @SerialName("balance_before")
    val balanceBefore: Double,
    @SerialName("balance_after")
    val balanceAfter: Double,
    val description: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
)
