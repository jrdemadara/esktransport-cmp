package org.noztek.esktransport.core.realtime

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.util.HttpChannelAuthorizer

private class AndroidRealtimeClient(
    private val pusher: Pusher,
    private val authorizer: HttpChannelAuthorizer,
    private val authTokenProvider: () -> String?,
) : RealtimeClient {
    override fun connect() = pusher.connect()

    override fun disconnect() = pusher.disconnect()

    override fun subscribePrivateChannel(
        channelName: String,
        eventName: String,
        onEvent: (eventName: String, data: String) -> Unit,
    ) {
        val channel = if (channelName.startsWith("private-")) channelName else "private-$channelName"
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
        pusher.subscribePrivate(channel, object : PrivateChannelEventListener {
            override fun onSubscriptionSucceeded(channelName: String?) = Unit
            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                println("Pusher auth failed for $channel: $message")
                e?.printStackTrace()
            }
            override fun onEvent(event: PusherEvent?) {
                if (event != null) onEvent(event.eventName, event.data)
            }
        }, eventName)
    }

    override fun unsubscribe(channelName: String) {
        val channel = if (channelName.startsWith("private-")) channelName else "private-$channelName"
        pusher.unsubscribe(channel)
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
