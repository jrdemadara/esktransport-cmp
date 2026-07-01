package org.noztek.esktransport.feature.driver.home.data.impl

import org.noztek.esktransport.feature.driver.home.data.remote.dto.DriverHomeStatsDto
import org.noztek.esktransport.feature.driver.home.data.remote.dto.DriverRatingDto
import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.home.domain.model.DriverRating

fun DriverHomeStatsDto.toDomain(): DriverHomeStats {
    return DriverHomeStats(
        totalTrips = totalTrips,
        onlineSeconds = onlineSeconds,
        rating = rating.toDomain(),
    )
}

fun DriverRatingDto.toDomain(): DriverRating {
    return DriverRating(
        value = value,
        label = label,
        max = max,
    )
}
