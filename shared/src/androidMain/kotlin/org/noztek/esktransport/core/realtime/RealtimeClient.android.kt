package org.noztek.esktransport.core.realtime

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.util.HttpChannelAuthorizer

private class AndroidRealtimeClient(
    private val pusher: Pusher,
    private val authorizer: HttpChannelAuthorizer,
    private val authTokenProvider: () -> String?,
) : RealtimeClient {
    private val channelEventCallbacks = mutableMapOf<String, MutableMap<String, MutableList<(String, String) -> Unit>>>()
    private val activeChannelListeners = mutableMapOf<String, PrivateChannelEventListener>()
    private val activePrivateChannels = mutableMapOf<String, PrivateChannel>()
    private val boundEventNames = mutableMapOf<String, MutableSet<String>>()

    override fun connect() = pusher.connect()

    override fun disconnect() = pusher.disconnect()

    override fun subscribePrivateChannel(
        channelName: String,
        eventName: String,
        onEvent: (eventName: String, data: String) -> Unit,
    ) {
        val channel = if (channelName.startsWith("private-")) channelName else "private-$channelName"
        val callbacks = channelEventCallbacks.getOrPut(channel) { mutableMapOf() }
            .getOrPut(eventName) { mutableListOf() }
        callbacks.add(onEvent)
        println("Realtime subscribe request: channel=$channel event=$eventName")

        if (activeChannelListeners.containsKey(channel)) {
            val privateChannel = activePrivateChannels[channel]
            val bound = boundEventNames.getOrPut(channel) { mutableSetOf() }
            if (privateChannel != null && bound.add(eventName)) {
                privateChannel.bind(eventName, activeChannelListeners[channel])
                println("Realtime bound extra event: channel=$channel event=$eventName")
            }
            return
        }

        authorizer.setHeaders(
            mutableMapOf(
                "Accept" to "application/json",
                "Content-Type" to "application/x-www-form-urlencoded",
                "X-Requested-With" to "XMLHttpRequest",
            ).apply {
                authTokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
                    put("Authorization", "Bearer $token")
                }
            },
        )
        val listener = object : PrivateChannelEventListener {
            override fun onSubscriptionSucceeded(channelName: String?) {
                println("Realtime subscribed: channel=$channelName")
            }
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                println("Pusher auth failed for $channel: $message")
                e?.printStackTrace()
            }
            override fun onEvent(event: PusherEvent?) {
                val name = event?.eventName ?: return
                val data = event.data
                println("Realtime event received: channel=$channel event=$name")
                channelEventCallbacks[channel]
                    ?.get(name)
                    ?.forEach { callback -> callback(name, data) }
            }
        }
        activeChannelListeners[channel] = listener
        val privateChannel = pusher.subscribePrivate(channel, listener)
        activePrivateChannels[channel] = privateChannel
        boundEventNames[channel] = mutableSetOf(eventName).apply {
            privateChannel.bind(eventName, listener)
        }
    }

    override fun unsubscribe(channelName: String) {
        val channel = if (channelName.startsWith("private-")) channelName else "private-$channelName"
        pusher.unsubscribe(channel)
        channelEventCallbacks.remove(channel)
        activeChannelListeners.remove(channel)
        activePrivateChannels.remove(channel)
        boundEventNames.remove(channel)
    }
}

actual fun createRealtimeClient(config: RealtimeConfig, authTokenProvider: () -> String?): RealtimeClient {
    val authorizer = HttpChannelAuthorizer(config.authEndpoint)

    val options = PusherOptions().apply {
        setCluster(config.cluster)
        channelAuthorizer = authorizer
    }

    val pusher = Pusher(config.appKey, options)
    return AndroidRealtimeClient(
        pusher = pusher,
        authorizer = authorizer,
        authTokenProvider = authTokenProvider,
    )
}
