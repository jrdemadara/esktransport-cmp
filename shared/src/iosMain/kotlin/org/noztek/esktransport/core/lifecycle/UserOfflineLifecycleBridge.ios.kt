package org.noztek.esktransport.core.lifecycle

internal object IosUserOfflineLifecycleBridge {
    var callback: (() -> Unit)? = null

    fun markOffline() {
        callback?.invoke()
    }
}

actual fun setPlatformUserOfflineCallback(callback: (() -> Unit)?) {
    IosUserOfflineLifecycleBridge.callback = callback
}
