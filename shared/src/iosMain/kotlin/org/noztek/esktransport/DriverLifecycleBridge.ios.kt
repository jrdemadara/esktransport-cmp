package org.noztek.esktransport

import org.noztek.esktransport.core.lifecycle.IosDriverOfflineLifecycleBridge

fun markDriverOfflineFromIosLifecycle() {
    IosDriverOfflineLifecycleBridge.markOffline()
}
