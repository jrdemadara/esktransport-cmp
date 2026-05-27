import CoreLocation
import MapboxMaps
import Shared
import UIKit

final class IosMapboxViewFactory: NSObject, Shared.IosMapboxViewFactory {
    func createMapView(request: IosMapboxViewRequest) -> UIView {
        MapboxOptions.accessToken = request.accessToken

        let styleURI = StyleURI(rawValue: request.styleUri) ?? .standard
        let options = MapInitOptions(styleURI: styleURI)
        let mapView = MapView(frame: .zero, mapInitOptions: options)
        applyCamera(to: mapView, request: request)
        return mapView
    }

    func updateMapView(view: UIView, request: IosMapboxViewRequest) {
        guard let mapView = view as? MapView else { return }
        MapboxOptions.accessToken = request.accessToken
        applyCamera(to: mapView, request: request)
    }

    private func applyCamera(to mapView: MapView, request: IosMapboxViewRequest) {
        let center = CLLocationCoordinate2D(
            latitude: request.latitude,
            longitude: request.longitude
        )
        mapView.mapboxMap.setCamera(
            to: CameraOptions(
                center: center,
                zoom: request.zoom,
                bearing: request.bearing,
                pitch: request.pitch
            )
        )
    }
}
