package org.noztek.esktransport.core.notify

actual fun createPushNotificationTokenProvider(): PushNotificationTokenProvider {
    return NoopPushNotificationTokenProvider
}

actual fun pushNotificationPlatform(): String = "ios"
