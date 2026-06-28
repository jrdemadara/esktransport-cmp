package org.noztek.esktransport

import org.noztek.esktransport.core.lifecycle.IosUserOfflineLifecycleBridge

fun markUserOfflineFromIosLifecycle() {
    IosUserOfflineLifecycleBridge.markOffline()
}
