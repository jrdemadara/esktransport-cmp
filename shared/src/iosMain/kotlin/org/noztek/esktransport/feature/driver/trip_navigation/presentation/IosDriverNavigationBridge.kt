package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import platform.UIKit.UIView
import org.noztek.esktransport.core.map.MapPoint

class IosDriverNavigationRequest(
    val accessToken: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val routePoints: List<MapPoint>,
    val pickupConfirmed: Boolean,
)

interface IosDriverNavigationViewFactory {
    fun createNavigationView(request: IosDriverNavigationRequest): UIView
    fun updateNavigationView(view: UIView, request: IosDriverNavigationRequest)
}

object IosDriverNavigationBridge {
    private var factory: IosDriverNavigationViewFactory? = null

    fun setFactory(factory: IosDriverNavigationViewFactory) {
        this.factory = factory
    }

    internal fun createNavigationView(request: IosDriverNavigationRequest): UIView? = factory?.createNavigationView(request)

    internal fun updateNavigationView(view: UIView, request: IosDriverNavigationRequest) {
        factory?.updateNavigationView(view, request)
    }
}
