package org.noztek.esktransport.feature.passenger.location_search.data.impl

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.ReverseGeocodeRepository

class ReverseGeocodeRepositoryImpl : ReverseGeocodeRepository {
    override suspend fun resolveLabel(point: GeoPoint): String {
        return "${point.latitude.toFixed(5)}, ${point.longitude.toFixed(5)}"
    }

    private fun Double.toFixed(decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = kotlin.math.round(this * factor) / factor
        return rounded.toString()
    }

    private fun pow10(decimals: Int): Double {
        var value = 1.0
        repeat(decimals) { value *= 10.0 }
        return value
    }
}
