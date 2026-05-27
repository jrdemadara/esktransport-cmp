package org.noztek.esktransport.feature.passenger.location_search.domain.usecase

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.ReverseGeocodeRepository

class ResolveTapLabelUseCase(
    private val reverseGeocodeRepository: ReverseGeocodeRepository,
) {
    suspend operator fun invoke(point: GeoPoint): String =
        reverseGeocodeRepository.resolveLabel(point) ?: point.coordinateLabel()

    private fun GeoPoint.coordinateLabel(): String = "${latitude.round5()}, ${longitude.round5()}"

    private fun Double.round5(): String {
        val factor = 100000.0
        return (kotlin.math.round(this * factor) / factor).toString()
    }
}
