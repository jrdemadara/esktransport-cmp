import Foundation
import PusherSwift
import Shared

final class PusherRealtimeClient {
    static let shared = PusherRealtimeClient()

    private var pusher: Pusher?
    private var subscribedChannels: [String: PusherChannel] = [:]
    private var authRequestBuilder: BearerAuthRequestBuilder?

    private init() {}

    func configure(
        appKey: String,
        cluster: String,
        authEndpoint: String,
        authTokenProvider: @escaping () -> String?
    ) {
        guard !appKey.isEmpty, !cluster.isEmpty, !authEndpoint.isEmpty else {
            RealtimeClient_iosKt.configureIosRealtimeBridge(
                connect: {},
                disconnect: {},
                subscribePrivateChannel: { _, _, _ in },
                unsubscribe: { _ in }
            )
            return
        }

        let authRequestBuilder = BearerAuthRequestBuilder(
            authEndpoint: authEndpoint,
            authTokenProvider: authTokenProvider
        )
        self.authRequestBuilder = authRequestBuilder

        let options = PusherClientOptions(
            authMethod: .authRequestBuilder(authRequestBuilder: authRequestBuilder),
            host: .cluster(cluster)
        )

        let pusher = Pusher(key: appKey, options: options)
        self.pusher = pusher

        RealtimeClient_iosKt.configureIosRealtimeBridge(
            connect: { [weak self] in
                self?.pusher?.connect()
            },
            disconnect: { [weak self] in
                self?.pusher?.disconnect()
            },
            subscribePrivateChannel: { [weak self] (channelName: String, eventName: String, onEvent: @escaping (String, String) -> KotlinUnit) in
                guard let self else { return }
                let normalized = channelName.hasPrefix("private-") ? channelName : "private-\(channelName)"

                let channel = self.pusher?.subscribe(normalized)
                self.subscribedChannels[normalized] = channel
                channel?.bind(eventName: eventName, eventCallback: { event in
                    let payload = event.data ?? ""
                    _ = onEvent(eventName, payload)
                })
            },
            unsubscribe: { [weak self] (channelName: String) in
                guard let self else { return }
                let normalized = channelName.hasPrefix("private-") ? channelName : "private-\(channelName)"
                self.pusher?.unsubscribe(normalized)
                self.subscribedChannels.removeValue(forKey: normalized)
            }
        )
    }
}

private final class BearerAuthRequestBuilder: AuthRequestBuilderProtocol {
    private let authEndpoint: String
    private let authTokenProvider: () -> String?

    init(
        authEndpoint: String,
        authTokenProvider: @escaping () -> String?
    ) {
        self.authEndpoint = authEndpoint
        self.authTokenProvider = authTokenProvider
    }

    func requestFor(socketID: String, channelName: String) -> URLRequest? {
        guard let url = URL(string: authEndpoint) else { return nil }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = "socket_id=\(socketID)&channel_name=\(channelName)".data(using: .utf8)
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        request.addValue("XMLHttpRequest", forHTTPHeaderField: "X-Requested-With")

        if let token = authTokenProvider(), !token.isEmpty {
            request.addValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        return request
    }
}
