package org.noztek.esktransport.core.realtime

interface BaseRealtimeCoordinator {
    fun connect()
    fun disconnect()
}

class DefaultBaseRealtimeCoordinator(
    private val realtimeClient: RealtimeClient,
) : BaseRealtimeCoordinator {
    override fun connect() = realtimeClient.connect()

    override fun disconnect() = realtimeClient.disconnect()
}
