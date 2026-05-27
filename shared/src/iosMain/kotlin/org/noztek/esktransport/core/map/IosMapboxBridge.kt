package org.noztek.esktransport.core.map

import platform.UIKit.UIView

class IosMapboxViewRequest(
    val accessToken: String,
    val styleUri: String,
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val pitch: Double,
    val bearing: Double,
)

interface IosMapboxViewFactory {
    fun createMapView(request: IosMapboxViewRequest): UIView

    fun updateMapView(view: UIView, request: IosMapboxViewRequest)
}

object IosMapboxBridge {
    private var factory: IosMapboxViewFactory? = null

    fun setFactory(factory: IosMapboxViewFactory) {
        this.factory = factory
    }

    internal fun createMapView(request: IosMapboxViewRequest): UIView? = factory?.createMapView(request)

    internal fun updateMapView(view: UIView, request: IosMapboxViewRequest) {
        factory?.updateMapView(view, request)
    }
}
