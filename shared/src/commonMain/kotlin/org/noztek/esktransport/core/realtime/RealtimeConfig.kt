package org.noztek.esktransport.core.realtime

data class RealtimeConfig(
    val appKey: String,
    val cluster: String,
    val authEndpoint: String,
)
