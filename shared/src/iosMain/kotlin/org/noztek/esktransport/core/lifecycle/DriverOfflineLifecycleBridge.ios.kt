package org.noztek.esktransport.core.lifecycle

internal object IosDriverOfflineLifecycleBridge {
    var callback: (() -> Unit)? = null

    fun markOffline() {
        callback?.invoke()
    }
}

actual fun setPlatformDriverOfflineCallback(callback: (() -> Unit)?) {
    IosDriverOfflineLifecycleBridge.callback = callback
}
