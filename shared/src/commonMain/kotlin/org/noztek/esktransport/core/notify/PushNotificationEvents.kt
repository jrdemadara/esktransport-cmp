package org.noztek.esktransport.core.notify

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class PushNotificationEvent(
    val title: String,
    val body: String,
)

object PushNotificationEvents {
    private val _events = MutableSharedFlow<PushNotificationEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<PushNotificationEvent> = _events.asSharedFlow()

    fun emit(title: String, body: String) {
        _events.tryEmit(PushNotificationEvent(title, body))
    }
}
