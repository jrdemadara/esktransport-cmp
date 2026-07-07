package org.noztek.esktransport.feature.driver.earning.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.earning.data.remote.RiderEarningsApi
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsSettlement
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsToday
import org.noztek.esktransport.feature.driver.earning.domain.repository.RiderEarningsRepository

class RiderEarningsRepositoryImpl(
    private val api: RiderEarningsApi,
) : RiderEarningsRepository {
    override suspend fun getEarnings(): Result<RiderEarningsDashboard> {
        return try {
            val data = api.getEarnings().data
            Result.success(
                RiderEarningsDashboard(
                    currency = data.currency,
                    today = RiderEarningsToday(
                        completedTrips = data.today.completedTrips,
                        grossFare = data.today.grossFare,
                        platformFee = data.today.platformFee,
                        netEarning = data.today.netEarning,
                    ),
                    recentSettlements = data.recentSettlements.map { settlement ->
                        RiderEarningsSettlement(
                            publicId = settlement.publicId,
                            grossFare = settlement.grossFare,
                            platformFee = settlement.platformFee,
                            netEarning = settlement.netEarning,
                            currency = settlement.currency,
                            settledAt = settlement.settledAt,
                        )
                    },
                ),
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load earnings.")
            Result.failure(IllegalStateException(message))
        }
    }
}
