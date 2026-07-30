package org.noztek.esktransport.feature.driver.wallet.data.impl

import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletDashboardDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletCashoutDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletLedgerEntryDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverWalletTopupDto
import org.noztek.esktransport.feature.driver.wallet.data.remote.dto.DriverModeWalletRequirementDto
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverModeWalletRequirement
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWallet
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletCashout
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletLedgerEntry
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup

fun DriverWalletDashboardDto.toDomain(): DriverWalletDashboard {
    return DriverWalletDashboard(
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

fun DriverWalletDto.toDomain(): DriverWallet {
    return DriverWallet(
        id = id,
        userId = userId,
        balance = balance,
        currency = currency,
        lastLedgerAt = lastLedgerAt,
    )
}

fun DriverWalletTopupDto.toDomain(): DriverWalletTopup {
    return DriverWalletTopup(
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

fun DriverWalletCashoutDto.toDomain(): DriverWalletCashout {
    return DriverWalletCashout(
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

fun DriverWalletLedgerEntryDto.toDomain(): DriverWalletLedgerEntry {
    return DriverWalletLedgerEntry(
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
