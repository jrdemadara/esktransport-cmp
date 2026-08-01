import CoreLocation
import MapboxMaps
import Shared
import UIKit
import MapboxNavigationUIKit
import MapboxNavigationCore



final class IosMapboxViewFactory: NSObject, Shared.IosMapboxViewFactory {
    private final class MapContainerView: UIView {
        let mapView: MapView
        var panObserver: PanObserver?
        var lastCameraCenter: CLLocationCoordinate2D?
        var antTimer: Timer?
        var styleLoadedCancelable: Cancelable?
        var userLocationCancelable: Cancelable?
        var hasCenteredOnUserLocation = false
        var lastAppliedCameraKey: String?
        var markerManager: CircleAnnotationManager?
        var iconMarkerManager: PointAnnotationManager?
        var staticRouteLayerIds: [String] = []
        var staticRouteSourceIds: [String] = []

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
        var onCameraMoving: ((Shared.MapPoint) -> Void)?
        var onCameraIdle: ((Shared.MapPoint) -> Void)?

        init(mapView: MapView) {
            self.mapView = mapView
        }

        @objc func handlePan(_ recognizer: UIPanGestureRecognizer) {
            guard let mapView else { return }
            let center = mapView.mapboxMap.cameraState.center
            let point = Shared.MapPoint(latitude: center.latitude, longitude: center.longitude)

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
        let mapView = MapView(frame: UIScreen.main.bounds, mapInitOptions: options)
        configureMapOrnaments(in: mapView)
        let container = MapContainerView(mapView: mapView)
        container.panObserver = PanObserver(mapView: mapView)
        if let observer = container.panObserver {
            observer.onCameraMoving = request.onCameraMoving
            observer.onCameraIdle = request.onCameraIdle
            mapView.gestures.panGestureRecognizer.addTarget(observer, action: #selector(PanObserver.handlePan(_:)))
        }
        container.styleLoadedCancelable = mapView.mapboxMap.onStyleLoaded.observeNext { [weak self, weak mapView, weak container] _ in
            guard let self, let mapView, let container else { return }
            self.applyStaticRoutes(to: mapView, container: container, request: request)
            self.applyAntPath(to: mapView, container: container, request: request)
            self.applyMarkers(to: mapView, container: container, request: request)
        }
        configureUserLocation(in: mapView, container: container, request: request)
        applyCamera(to: mapView, container: container, request: request, force: true)
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
        configureMapOrnaments(in: mapView)
        container.panObserver?.onCameraMoving = request.onCameraMoving
        container.panObserver?.onCameraIdle = request.onCameraIdle
        configureUserLocation(in: mapView, container: container, request: request)
        if request.syncCameraPosition {
            applyCamera(to: mapView, container: container, request: request, force: false)
        }
        applyStaticRoutes(to: mapView, container: container, request: request)
        applyAntPath(to: mapView, container: container, request: request)
        applyMarkers(to: mapView, container: container, request: request)
        container.lastCameraCenter = CLLocationCoordinate2D(latitude: request.latitude, longitude: request.longitude)
    }

    private func configureMapOrnaments(in mapView: MapView) {
        mapView.ornaments.options.scaleBar.visibility = .hidden
    }

    private func configureUserLocation(
        in mapView: MapView,
        container: MapContainerView,
        request: IosMapboxViewRequest
    ) {
        guard request.showUserLocation else {
            mapView.location.options.puckType = nil
            container.userLocationCancelable?.cancel()
            container.userLocationCancelable = nil
            container.hasCenteredOnUserLocation = false
            return
        }

        var puck = Puck2DConfiguration(
            topImage: driverLocationPuckImage(color: driverPrimaryColor),
            pulsing: .init(
                color: driverPrimaryColor.withAlphaComponent(0.42),
                radius: .constant(38)
            )
        )
        puck.showsAccuracyRing = false
        mapView.location.options.puckType = .puck2D(puck)

        guard container.userLocationCancelable == nil else { return }
        container.userLocationCancelable = mapView.location.onLocationChange.observeNext { [weak mapView, weak container] locations in
            guard let mapView, let container, !container.hasCenteredOnUserLocation,
                  let location = locations.last else { return }

            container.hasCenteredOnUserLocation = true
            mapView.camera.ease(
                to: CameraOptions(center: location.coordinate, zoom: request.zoom),
                duration: 0.45
            )
        }
    }

    private func applyMarkers(to mapView: MapView, container: MapContainerView, request: IosMapboxViewRequest) {
        guard !request.markers.isEmpty else {
            if container.markerManager != nil {
                mapView.annotations.removeAnnotationManager(withId: "ios-map-markers")
                container.markerManager = nil
            }
            if container.iconMarkerManager != nil {
                mapView.annotations.removeAnnotationManager(withId: "ios-icon-markers")
                container.iconMarkerManager = nil
            }
            return
        }

        // Recreate managers after route layers so markers stay above the animated ant path.
        if container.markerManager != nil {
            mapView.annotations.removeAnnotationManager(withId: "ios-map-markers")
        }
        if container.iconMarkerManager != nil {
            mapView.annotations.removeAnnotationManager(withId: "ios-icon-markers")
        }

        let circleManager = mapView.annotations.makeCircleAnnotationManager(id: "ios-map-markers")
        let iconManager = mapView.annotations.makePointAnnotationManager(id: "ios-icon-markers")
        container.markerManager = circleManager
        container.iconMarkerManager = iconManager

        circleManager.annotations = request.markers.flatMap { marker -> [CircleAnnotation] in
            guard marker.iconName == nil else { return [] }
            let coordinate = CLLocationCoordinate2D(
                latitude: marker.point.latitude,
                longitude: marker.point.longitude
            )

            var outerGlow = CircleAnnotation(centerCoordinate: coordinate)
            outerGlow.circleColor = StyleColor(UIColor.white)
            outerGlow.circleOpacity = 0.48
            outerGlow.circleRadius = 18.0

            var innerGlow = CircleAnnotation(centerCoordinate: coordinate)
            innerGlow.circleColor = StyleColor(UIColor.white)
            innerGlow.circleOpacity = 0.86
            innerGlow.circleRadius = 12.0

            var core = CircleAnnotation(centerCoordinate: coordinate)
            core.circleColor = StyleColor(UIColor(hex: marker.colorHex) ?? UIColor(red: 0.145, green: 0.388, blue: 0.922, alpha: 1.0))
            core.circleRadius = 7.0

            return [outerGlow, innerGlow, core]
        }

        iconManager.annotations = request.markers.compactMap { marker in
            guard let iconName = marker.iconName else { return nil }
            let coordinate = CLLocationCoordinate2D(
                latitude: marker.point.latitude,
                longitude: marker.point.longitude
            )
            guard let image = UIImage.composeResourceImage(named: iconName, type: "png") else { return nil }

            var annotation = PointAnnotation(coordinate: coordinate)
            annotation.image = .init(image: image, name: iconName)
            annotation.iconAnchor = .bottom
            annotation.iconSize = iconSize(for: iconName)
            return annotation
        }
    }

    private func iconSize(for iconName: String) -> Double {
        switch iconName {
        case "flag":
            return 0.12
        case "driver_marker", "passenger_marker":
            return 0.13
        default:
            return 0.13
        }
    }

    private func applyStaticRoutes(to mapView: MapView, container: MapContainerView, request: IosMapboxViewRequest) {
        removeStaticRoutes(from: mapView, container: container)

        request.routeLines
            .filter { !$0.animated && $0.points.count >= 2 }
            .forEach { route in
                let sourceId = "ios-route-\(route.id)-source"
                let layerId = "ios-route-\(route.id)-layer"
                let coordinates = route.points.map { CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude) }
                let line = LineString(coordinates)
                let feature = Feature(geometry: .lineString(line))
                let featureCollection = FeatureCollection(features: [feature])

                var source = GeoJSONSource(id: sourceId)
                source.data = .featureCollection(featureCollection)
                try? mapView.mapboxMap.addSource(source)

                var layer = LineLayer(id: layerId, source: sourceId)
                layer.lineColor = .constant(StyleColor(UIColor(hex: route.colorHex) ?? driverPrimaryColor))
                layer.lineWidth = .constant(route.width)
                layer.lineOpacity = .constant(route.opacity)
                layer.lineCap = .constant(.round)
                layer.lineJoin = .constant(.round)
                if !route.dashPattern.isEmpty {
                    layer.lineDasharray = .constant(route.dashPattern.map { Double(truncating: $0) })
                }
                try? mapView.mapboxMap.addLayer(layer)

                container.staticRouteSourceIds.append(sourceId)
                container.staticRouteLayerIds.append(layerId)
            }
    }

    private func removeStaticRoutes(from mapView: MapView, container: MapContainerView) {
        container.staticRouteLayerIds.forEach { layerId in
            if mapView.mapboxMap.layerExists(withId: layerId) {
                try? mapView.mapboxMap.removeLayer(withId: layerId)
            }
        }
        container.staticRouteSourceIds.forEach { sourceId in
            if mapView.mapboxMap.sourceExists(withId: sourceId) {
                try? mapView.mapboxMap.removeSource(withId: sourceId)
            }
        }
        container.staticRouteLayerIds.removeAll()
        container.staticRouteSourceIds.removeAll()
    }

    private func applyAntPath(to mapView: MapView, container: MapContainerView, request: IosMapboxViewRequest) {
        container.antTimer?.invalidate()
        container.antTimer = nil
        let sourceId = "ios-ant-source"
        let bgLayerId = "ios-ant-bg"
        let dashLayerId = "ios-ant-dash"
        guard request.antPathEnabled, request.routePoints.count >= 2 else {
            removeAntPath(from: mapView, sourceId: sourceId, bgLayerId: bgLayerId, dashLayerId: dashLayerId)
            return
        }

        let coordinates = request.routePoints.map { CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude) }
        let line = LineString(coordinates)
        let feature = Feature(geometry: .lineString(line))
        let fc = FeatureCollection(features: [feature])

        if mapView.mapboxMap.sourceExists(withId: sourceId) {
            mapView.mapboxMap.updateGeoJSONSource(withId: sourceId, geoJSON: .featureCollection(fc))
        } else {
            var source = GeoJSONSource(id: sourceId)
            source.data = .featureCollection(fc)
            try? mapView.mapboxMap.addSource(source)
        }

        if mapView.mapboxMap.layerExists(withId: bgLayerId) {
            try? mapView.mapboxMap.removeLayer(withId: bgLayerId)
        }
        if mapView.mapboxMap.layerExists(withId: dashLayerId) {
            try? mapView.mapboxMap.removeLayer(withId: dashLayerId)
        }

        if !mapView.mapboxMap.layerExists(withId: bgLayerId) {
            var bg = LineLayer(id: bgLayerId, source: sourceId)
            bg.lineColor = .constant(StyleColor(UIColor(hex: request.antPathColorHex) ?? UIColor(red: 0.145, green: 0.388, blue: 0.922, alpha: 1.0)))
            bg.lineWidth = .constant(request.antPathWidth)
            bg.lineOpacity = .constant(0.0)
            bg.lineCap = .constant(.butt)
            bg.lineJoin = .constant(.bevel)
            try? mapView.mapboxMap.addLayer(bg)
        }
        if !mapView.mapboxMap.layerExists(withId: dashLayerId) {
            var dash = LineLayer(id: dashLayerId, source: sourceId)
            dash.lineColor = .constant(StyleColor(UIColor(hex: request.antPathColorHex) ?? UIColor(red: 0.145, green: 0.388, blue: 0.922, alpha: 1.0)))
            dash.lineWidth = .constant(request.antPathWidth)
            dash.lineDasharray = .constant([0.0, 4.0, 3.0])
            dash.lineCap = .constant(.butt)
            dash.lineJoin = .constant(.bevel)
            try? mapView.mapboxMap.addLayer(dash)
        }

        let sequence: [[Double]] = [
            [0.0, 4.0, 3.0], [0.5, 4.0, 2.5], [1.0, 4.0, 2.0], [1.5, 4.0, 1.5],
            [2.0, 4.0, 1.0], [2.5, 4.0, 0.5], [3.0, 4.0, 0.0], [0.0, 0.5, 3.0, 3.5],
            [0.0, 1.0, 3.0, 3.0], [0.0, 1.5, 3.0, 2.5], [0.0, 2.0, 3.0, 2.0], [0.0, 2.5, 3.0, 1.5],
            [0.0, 3.0, 3.0, 1.0], [0.0, 3.5, 3.0, 0.5]
        ]
        var step = 0
        container.antTimer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true) { [weak mapView] _ in
            guard let mapView else { return }
            let dashValues = sequence[step]
            do {
                try mapView.mapboxMap.updateLayer(withId: dashLayerId, type: LineLayer.self) { layer in
                    layer.lineDasharray = .constant(dashValues)
                }
            } catch {
                // ignore transient style/layer update errors during lifecycle changes
            }
            step = (step + 1) % sequence.count
        }
    }

    private func removeAntPath(from mapView: MapView, sourceId: String, bgLayerId: String, dashLayerId: String) {
        if mapView.mapboxMap.layerExists(withId: dashLayerId) {
            try? mapView.mapboxMap.removeLayer(withId: dashLayerId)
        }
        if mapView.mapboxMap.layerExists(withId: bgLayerId) {
            try? mapView.mapboxMap.removeLayer(withId: bgLayerId)
        }
        if mapView.mapboxMap.sourceExists(withId: sourceId) {
            try? mapView.mapboxMap.removeSource(withId: sourceId)
        }
    }

    private func applyCamera(
        to mapView: MapView,
        container: MapContainerView,
        request: IosMapboxViewRequest,
        force: Bool
    ) {
        let cameraKey = cameraKey(for: request)
        if !force && container.lastAppliedCameraKey == cameraKey {
            return
        }

        if request.routePoints.count >= 2 {
            let coordinates = request.routePoints.map {
                CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude)
            }
            let camera = CameraOptions(
                bearing: request.bearing,
                pitch: request.pitch
            )
            let padding = UIEdgeInsets(top: 100.0, left: 80.0, bottom: 410.0, right: 80.0)
            if let routeCamera = try? mapView.mapboxMap.camera(
                for: coordinates,
                camera: camera,
                coordinatesPadding: padding,
                maxZoom: 16.0,
                offset: nil
            ) {
                var adjustedCamera = routeCamera
                adjustedCamera.zoom = (routeCamera.zoom ?? request.zoom) + 0.35
                mapView.mapboxMap.setCamera(to: adjustedCamera)
                container.lastAppliedCameraKey = cameraKey
                return
            }
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
        container.lastAppliedCameraKey = cameraKey
    }

    private func cameraKey(for request: IosMapboxViewRequest) -> String {
        let routeKey = request.routePoints.map { "\($0.latitude),\($0.longitude)" }.joined(separator: "|")
        return "\(request.latitude),\(request.longitude),\(request.zoom),\(request.pitch),\(request.bearing),\(routeKey)"
    }
}

private let driverPrimaryColor = UIColor(red: 0.0, green: 0.294, blue: 0.831, alpha: 1.0)

private func driverLocationPuckImage(color: UIColor) -> UIImage {
    let size = CGSize(width: 28, height: 28)
    let renderer = UIGraphicsImageRenderer(size: size)
    return renderer.image { _ in
        UIColor.white.setFill()
        UIBezierPath(ovalIn: CGRect(x: 1, y: 1, width: 26, height: 26)).fill()
        color.setFill()
        UIBezierPath(ovalIn: CGRect(x: 5, y: 5, width: 18, height: 18)).fill()
    }
}

private extension UIColor {
    convenience init?(hex: String) {
        var cleaned = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if cleaned.hasPrefix("#") { cleaned.removeFirst() }
        guard cleaned.count == 6, let value = Int(cleaned, radix: 16) else { return nil }
        let r = CGFloat((value >> 16) & 0xFF) / 255.0
        let g = CGFloat((value >> 8) & 0xFF) / 255.0
        let b = CGFloat(value & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b, alpha: 1.0)
    }
}

private extension UIImage {
    static func composeResourceImage(named name: String, type: String) -> UIImage? {
        let directories = [
            "compose-resources/composeResources/esktransport.shared.generated.resources/drawable",
            "compose-resources/composeResources/asktransport_cmp.shared.generated.resources/drawable",
            "composeResources/esktransport.shared.generated.resources/drawable",
            "composeResources/asktransport_cmp.shared.generated.resources/drawable",
        ]

        for directory in directories {
            if let path = Bundle.main.path(forResource: name, ofType: type, inDirectory: directory),
               let image = UIImage(contentsOfFile: path) {
                return image
            }
        }

        return UIImage(named: name)
    }
}
