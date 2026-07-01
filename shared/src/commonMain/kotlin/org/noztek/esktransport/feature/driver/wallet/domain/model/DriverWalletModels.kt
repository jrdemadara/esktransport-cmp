package org.noztek.esktransport.feature.driver.wallet.domain.model

data class DriverWalletDashboard(
    val wallet: DriverWallet,
    val recentLedgerEntries: List<DriverWalletLedgerEntry>,
    val pendingTopups: List<DriverWalletTopup>,
)

data class DriverWallet(
    val id: Long,
    val userId: Long,
    val balance: Double,
    val currency: String,
    val lastLedgerAt: String?,
)

data class DriverWalletTopup(
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

data class DriverWalletLedgerEntry(
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
