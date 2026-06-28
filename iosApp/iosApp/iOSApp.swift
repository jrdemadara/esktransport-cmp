import SwiftUI
import Shared
import UIKit

final class AppDelegate: NSObject, UIApplicationDelegate {
    func applicationWillTerminate(_ application: UIApplication) {
        DriverLifecycleBridge_iosKt.markDriverOfflineFromIosLifecycle()
        UserLifecycleBridge_iosKt.markUserOfflineFromIosLifecycle()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase

    init() {
        let appKey = Bundle.main.object(forInfoDictionaryKey: "PusherAppKey") as? String ?? ""
        let cluster = Bundle.main.object(forInfoDictionaryKey: "PusherAppCluster") as? String ?? ""
        let authEndpoint = Bundle.main.object(forInfoDictionaryKey: "PusherAuthEndpoint") as? String ?? ""

        PusherRealtimeClient.shared.configure(
            appKey: appKey,
            cluster: cluster,
            authEndpoint: authEndpoint,
            authTokenProvider: {
                IosAuthTokenBridge.shared.cachedToken()
            }
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .background {
                DriverLifecycleBridge_iosKt.markDriverOfflineFromIosLifecycle()
                UserLifecycleBridge_iosKt.markUserOfflineFromIosLifecycle()
            }
        }
    }
}
