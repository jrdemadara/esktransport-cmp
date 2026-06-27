 import UIKit
import SwiftUI
import Shared

private let appBackgroundColor = Color(
    UIColor { traitCollection in
        if traitCollection.userInterfaceStyle == .dark {
            return UIColor(red: 0x11 / 255.0, green: 0x13 / 255.0, blue: 0x1B / 255.0, alpha: 1.0)
        }

        return UIColor(red: 0xF6 / 255.0, green: 0xF9 / 255.0, blue: 0xFF / 255.0, alpha: 1.0)
    }
)

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
        ZStack {
            appBackgroundColor
                .ignoresSafeArea()
            ComposeView()
                .ignoresSafeArea(.container, edges: [.top, .bottom])
        }
        .ignoresSafeArea(.keyboard)
    }
}
