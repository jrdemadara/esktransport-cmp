import CoreLocation
import MapboxNavigationCore
import MapboxNavigationUIKit
import Shared
import UIKit

final class IosDriverNavigationViewFactory: NSObject, Shared.IosDriverNavigationViewFactory {
    private final class EmptyBottomBannerViewController: ContainerViewController {
        override func loadView() {
            let view = UIView(frame: .zero)
            view.backgroundColor = .clear
            view.isHidden = true
            self.view = view
            preferredContentSize = .zero
        }
    }

    @MainActor
    private static var sharedNavigationOptions: NavigationOptions?

    private final class Container: UIView {
        var currentRequest: IosDriverNavigationRequest
        var navigationController: NavigationViewController?
        var currentStageKey: String?
        var isFinalStage: Bool = false
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

    private static func parentViewController(for view: UIView) -> UIViewController? {
        var responder: UIResponder? = view
        while let current = responder {
            if let vc = current as? UIViewController { return vc }
            responder = current.next
        }
        return nil
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
        container.isFinalStage = request.pickupConfirmed
        if container.currentStageKey == stageKey, container.navigationController != nil {
            return
        }
        container.currentStageKey = stageKey

        let pickup = CLLocationCoordinate2D(latitude: request.pickupLatitude, longitude: request.pickupLongitude)
        let destination = CLLocationCoordinate2D(latitude: request.destinationLatitude, longitude: request.destinationLongitude)
        let currentDriverLocation = CLLocationManager().location?.coordinate
        let from = request.pickupConfirmed
            ? pickup
            : (currentDriverLocation ?? pickup)
        let to = request.pickupConfirmed ? destination : pickup

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
                    let options = NavigationOptions()
                    options.bottomBanner = EmptyBottomBannerViewController()
                    Self.sharedNavigationOptions = options
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
                    navVC.showsEndOfRouteFeedback = container.isFinalStage

                    if let existing = container.navigationController {
                        existing.willMove(toParent: nil)
                        existing.view.removeFromSuperview()
                        existing.removeFromParent()
                    }

                    container.navigationController = navVC

                    if let parentVC = Self.parentViewController(for: container) {
                        parentVC.addChild(navVC)
                    }

                    navVC.view.translatesAutoresizingMaskIntoConstraints = false
                    container.addSubview(navVC.view)
                    NSLayoutConstraint.activate([
                        navVC.view.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                        navVC.view.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                        navVC.view.topAnchor.constraint(equalTo: container.topAnchor),
                        navVC.view.bottomAnchor.constraint(equalTo: container.bottomAnchor),
                    ])
                    navVC.didMove(toParent: Self.parentViewController(for: container))
                    container.bringSubviewToFront(container.loading)
                    container.loading.stopAnimating()
                }
            }
        }
    }
}
