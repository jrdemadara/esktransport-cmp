import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        let mapboxAccessToken = Bundle.main.object(forInfoDictionaryKey: "MBXAccessToken") as? String ?? ""
        IosMapboxBridge.shared.setFactory(factory: IosMapboxViewFactory())
        IosDriverNavigationBridge.shared.setFactory(factory: IosDriverNavigationViewFactory())
        return MainViewControllerKt.MainViewController(mapboxAccessToken: mapboxAccessToken)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.container, edges: .bottom)

    }
}
