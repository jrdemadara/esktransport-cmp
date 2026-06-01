import CoreLocation
import MapboxNavigationCore
import MapboxNavigationUIKit
import Shared
import UIKit

final class IosDriverNavigationViewFactory: NSObject, Shared.IosDriverNavigationViewFactory {
    @MainActor
    private static var sharedNavigationOptions: NavigationOptions?

    private final class Container: UIView {
        var currentRequest: IosDriverNavigationRequest
        var navigationController: NavigationViewController?
        var currentStageKey: String?
        var routeTask: Task<Void, Never>?
        let loading = UIActivityIndicatorView(style: .large)

        init(request: IosDriverNavigationRequest) {
            self.currentRequest = request
            super.init(frame: .zero)
            backgroundColor = .black
            loading.translatesAutoresizingMaskIntoConstraints = false
            loading.hidesWhenStopped = true
            addSubview(loading)
            NSLayoutConstraint.activate([
                loading.centerXAnchor.constraint(equalTo: centerXAnchor),
                loading.centerYAnchor.constraint(equalTo: centerYAnchor),
            ])
        }

        @available(*, unavailable)
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }

    func createNavigationView(request: IosDriverNavigationRequest) -> UIView {
        let container = Container(request: request)
        updateNavigationView(view: container, request: request)
        return container
    }

    func updateNavigationView(view: UIView, request: IosDriverNavigationRequest) {
        guard let container = view as? Container else { return }
        container.currentRequest = request

        let stageKey = request.pickupConfirmed ? "to-destination" : "to-pickup"
        if container.currentStageKey == stageKey, container.navigationController != nil {
            return
        }
        container.currentStageKey = stageKey

        let from = CLLocationCoordinate2D(latitude: request.pickupLatitude, longitude: request.pickupLongitude)
        let to = CLLocationCoordinate2D(latitude: request.destinationLatitude, longitude: request.destinationLongitude)

        mountNavigation(in: container, from: from, to: to)
    }

    private func mountNavigation(
        in container: Container,
        from: CLLocationCoordinate2D,
        to: CLLocationCoordinate2D
    ) {
        container.routeTask?.cancel()
        let routeOptions = NavigationRouteOptions(coordinates: [from, to])
        container.loading.startAnimating()

        container.routeTask = Task { [weak container] in
            guard let container else { return }
            let (navigationOptions, routingProvider): (NavigationOptions, RoutingProvider) = await MainActor.run {
                if Self.sharedNavigationOptions == nil {
                    Self.sharedNavigationOptions = NavigationOptions()
                }
                let options = Self.sharedNavigationOptions!
                let provider = options.mapboxNavigation.routingProvider()
                return (options, provider)
            }

            let request = routingProvider.calculateRoutes(options: routeOptions)
            switch await request.result {
            case .failure:
                await MainActor.run { container.loading.stopAnimating() }
                return
            case .success(let navigationRoutes):
                await MainActor.run {
                    let navVC = NavigationViewController(
                        navigationRoutes: navigationRoutes,
                        navigationOptions: navigationOptions
                    )
                    navVC.routeLineTracksTraversal = true

                    container.navigationController?.willMove(toParent: nil)
                    container.navigationController?.view.removeFromSuperview()
                    container.navigationController?.removeFromParent()

                    container.navigationController = navVC

                    navVC.view.translatesAutoresizingMaskIntoConstraints = false
                    container.addSubview(navVC.view)
                    NSLayoutConstraint.activate([
                        navVC.view.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                        navVC.view.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                        navVC.view.topAnchor.constraint(equalTo: container.topAnchor),
                        navVC.view.bottomAnchor.constraint(equalTo: container.bottomAnchor),
                    ])
                    container.bringSubviewToFront(container.loading)
                    container.loading.stopAnimating()
                }
            }
        }
    }
}
