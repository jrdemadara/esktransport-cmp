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
    val routePoints: List<MapPoint>,
    val markers: List<IosMapMarkerRequest>,
    val antPathEnabled: Boolean,
    val antPathColorHex: String,
    val antPathWidth: Double,
    val onCameraMoving: ((MapPoint) -> Unit)?,
    val onCameraIdle: ((MapPoint) -> Unit)?,
)

class IosMapMarkerRequest(
    val id: String,
    val point: MapPoint,
    val colorHex: String,
    val radius: Double,
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
