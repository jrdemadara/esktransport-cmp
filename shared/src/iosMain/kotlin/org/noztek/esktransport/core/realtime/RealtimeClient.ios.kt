package org.noztek.esktransport.core.realtime

private object IosRealtimeBridge {
    var connect: (() -> Unit)? = null
    var disconnect: (() -> Unit)? = null
    var subscribePrivateChannel: ((String, String, (String, String) -> Unit) -> Unit)? = null
    var unsubscribe: ((String) -> Unit)? = null
}

fun configureIosRealtimeBridge(
    connect: (() -> Unit)?,
    disconnect: (() -> Unit)?,
    subscribePrivateChannel: ((channelName: String, eventName: String, onEvent: (eventName: String, data: String) -> Unit) -> Unit)?,
    unsubscribe: ((channelName: String) -> Unit)?,
) {
    IosRealtimeBridge.connect = connect
    IosRealtimeBridge.disconnect = disconnect
    IosRealtimeBridge.subscribePrivateChannel = subscribePrivateChannel
    IosRealtimeBridge.unsubscribe = unsubscribe
}

private class IosRealtimeClient : RealtimeClient {
    override fun connect() = IosRealtimeBridge.connect?.invoke() ?: Unit
    override fun disconnect() = IosRealtimeBridge.disconnect?.invoke() ?: Unit
    override fun subscribePrivateChannel(
        channelName: String,
        eventName: String,
        onEvent: (eventName: String, data: String) -> Unit,
    ) = IosRealtimeBridge.subscribePrivateChannel?.invoke(channelName, eventName, onEvent) ?: Unit

    override fun unsubscribe(channelName: String) = IosRealtimeBridge.unsubscribe?.invoke(channelName) ?: Unit
}

actual fun createRealtimeClient(config: RealtimeConfig, authTokenProvider: () -> String?): RealtimeClient {
    return IosRealtimeClient()
}
