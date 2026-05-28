import CoreLocation
import MapboxMaps
import Shared
import UIKit

final class IosMapboxViewFactory: NSObject, Shared.IosMapboxViewFactory {
    private final class MapContainerView: UIView {
        let mapView: MapView
        var panObserver: PanObserver?
        var lastCameraCenter: CLLocationCoordinate2D?

        init(mapView: MapView) {
            self.mapView = mapView
            super.init(frame: .zero)
            addSubview(mapView)
            mapView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                mapView.leadingAnchor.constraint(equalTo: leadingAnchor),
                mapView.trailingAnchor.constraint(equalTo: trailingAnchor),
                mapView.topAnchor.constraint(equalTo: topAnchor),
                mapView.bottomAnchor.constraint(equalTo: bottomAnchor),
            ])
        }

        @available(*, unavailable)
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }

    private final class PanObserver: NSObject {
        weak var mapView: MapView?
        var onCameraMoving: ((MapPoint) -> Void)?
        var onCameraIdle: ((MapPoint) -> Void)?

        init(mapView: MapView) {
            self.mapView = mapView
        }

        @objc func handlePan(_ recognizer: UIPanGestureRecognizer) {
            guard let mapView else { return }
            let center = mapView.mapboxMap.cameraState.center
            let point = MapPoint(latitude: center.latitude, longitude: center.longitude)

            switch recognizer.state {
            case .began, .changed:
                onCameraMoving?(point)
            case .ended, .cancelled, .failed:
                onCameraIdle?(point)
            default:
                break
            }
        }
    }

    func createMapView(request: IosMapboxViewRequest) -> UIView {
        MapboxOptions.accessToken = request.accessToken

        let styleURI = StyleURI(rawValue: request.styleUri) ?? .standard
        let options = MapInitOptions(styleURI: styleURI)
        let mapView = MapView(frame: .zero, mapInitOptions: options)
        let container = MapContainerView(mapView: mapView)
        container.panObserver = PanObserver(mapView: mapView)
        if let observer = container.panObserver {
            observer.onCameraMoving = request.onCameraMoving
            observer.onCameraIdle = request.onCameraIdle
            mapView.gestures.panGestureRecognizer.addTarget(observer, action: #selector(PanObserver.handlePan(_:)))
        }
        applyCamera(to: mapView, request: request, lastCenter: nil)
        container.lastCameraCenter = CLLocationCoordinate2D(latitude: request.latitude, longitude: request.longitude)
        return container
    }

    func updateMapView(view: UIView, request: IosMapboxViewRequest) {
        guard let container = view as? MapContainerView else { return }
        let mapView = container.mapView
        MapboxOptions.accessToken = request.accessToken
        let styleURI = StyleURI(rawValue: request.styleUri) ?? .standard
        if mapView.mapboxMap.styleURI != styleURI {
            mapView.mapboxMap.loadStyle(styleURI)
        }
        container.panObserver?.onCameraMoving = request.onCameraMoving
        container.panObserver?.onCameraIdle = request.onCameraIdle
        applyCamera(to: mapView, request: request, lastCenter: container.lastCameraCenter)
        container.lastCameraCenter = CLLocationCoordinate2D(latitude: request.latitude, longitude: request.longitude)
    }

    private func applyCamera(
        to mapView: MapView,
        request: IosMapboxViewRequest,
        lastCenter: CLLocationCoordinate2D?
    ) {
        let newCenter = CLLocationCoordinate2D(latitude: request.latitude, longitude: request.longitude)
        if let lastCenter {
            let sameCenter = abs(lastCenter.latitude - newCenter.latitude) < 0.0000001 &&
                abs(lastCenter.longitude - newCenter.longitude) < 0.0000001
            if sameCenter { return }
        }
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
