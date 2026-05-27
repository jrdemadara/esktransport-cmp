package org.noztek.esktransport.core.realtime

interface RealtimeClient {
    fun connect()
    fun disconnect()

    fun subscribePrivateChannel(
        channelName: String,
        eventName: String,
        onEvent: (eventName: String, data: String) -> Unit,
    )

    fun unsubscribe(channelName: String)
}

expect fun createRealtimeClient(config: RealtimeConfig, authTokenProvider: () -> String?): RealtimeClient
