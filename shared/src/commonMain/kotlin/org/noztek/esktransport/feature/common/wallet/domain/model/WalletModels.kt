package org.noztek.esktransport.feature.common.wallet.domain.model

data class WalletDashboard(
    val wallet: Wallet,
    val driverModeRequirement: DriverModeWalletRequirement,
    val recentLedgerEntries: List<WalletLedgerEntry>,
    val pendingTopups: List<WalletTopup>,
    val pendingCashouts: List<WalletCashout>,
)

data class DriverModeWalletRequirement(
    val currency: String,
    val minimumWalletBalance: Double,
    val hasMinimumWalletBalance: Boolean,
)

data class Wallet(
    val id: Long,
    val userId: Long,
    val balance: Double,
    val currency: String,
    val lastLedgerAt: String?,
)

data class WalletTopup(
    val publicId: String,
    val referenceCode: String,
    val qrPayload: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val expiresAt: String?,
    val completedAt: String?,
    val createdAt: String?,
)

data class WalletCashout(
    val publicId: String,
    val referenceCode: String,
    val qrPayload: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val expiresAt: String?,
    val completedAt: String?,
    val createdAt: String?,
)

data class WalletLedgerEntry(
    val publicId: String,
    val entryType: String,
    val direction: String,
    val amount: Double,
    val currency: String,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val description: String?,
    val createdAt: String?,
)
