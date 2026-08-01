package org.noztek.esktransport.feature.common.wallet.data.impl

import org.noztek.esktransport.feature.common.wallet.data.remote.dto.DriverModeWalletRequirementDto
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletCashoutDto
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletDashboardDto
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletDto
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletLedgerEntryDto
import org.noztek.esktransport.feature.common.wallet.data.remote.dto.WalletTopupDto
import org.noztek.esktransport.feature.common.wallet.domain.model.DriverModeWalletRequirement
import org.noztek.esktransport.feature.common.wallet.domain.model.Wallet
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletCashout
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletDashboard
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletLedgerEntry
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletTopup

fun WalletDashboardDto.toDomain(): WalletDashboard {
    return WalletDashboard(
        wallet = wallet.toDomain(),
        driverModeRequirement = driverModeRequirement.toDomain(),
        recentLedgerEntries = recentLedgerEntries.map { it.toDomain() },
        pendingTopups = pendingTopups.map { it.toDomain() },
        pendingCashouts = pendingCashouts.map { it.toDomain() },
    )
}

fun DriverModeWalletRequirementDto.toDomain(): DriverModeWalletRequirement {
    return DriverModeWalletRequirement(
        currency = currency,
        minimumWalletBalance = minimumWalletBalance,
        hasMinimumWalletBalance = hasMinimumWalletBalance,
    )
}

fun WalletDto.toDomain(): Wallet {
    return Wallet(
        id = id,
        userId = userId,
        balance = balance,
        currency = currency,
        lastLedgerAt = lastLedgerAt,
    )
}

fun WalletTopupDto.toDomain(): WalletTopup {
    return WalletTopup(
        publicId = publicId,
        referenceCode = referenceCode,
        qrPayload = qrPayload,
        amount = amount,
        currency = currency,
        status = status,
        expiresAt = expiresAt,
        completedAt = completedAt,
        createdAt = createdAt,
    )
}

fun WalletCashoutDto.toDomain(): WalletCashout {
    return WalletCashout(
        publicId = publicId,
        referenceCode = referenceCode,
        qrPayload = qrPayload,
        amount = amount,
        currency = currency,
        status = status,
        expiresAt = expiresAt,
        completedAt = completedAt,
        createdAt = createdAt,
    )
}

fun WalletLedgerEntryDto.toDomain(): WalletLedgerEntry {
    return WalletLedgerEntry(
        publicId = publicId,
        entryType = entryType,
        direction = direction,
        amount = amount,
        currency = currency,
        balanceBefore = balanceBefore,
        balanceAfter = balanceAfter,
        description = description,
        createdAt = createdAt,
    )
}
