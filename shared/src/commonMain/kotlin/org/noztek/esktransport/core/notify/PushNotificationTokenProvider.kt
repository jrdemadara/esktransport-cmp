package org.noztek.esktransport.core.notify

interface PushNotificationTokenProvider {
    suspend fun currentToken(): Result<String>
}

internal object NoopPushNotificationTokenProvider : PushNotificationTokenProvider {
    override suspend fun currentToken(): Result<String> {
        return Result.failure(UnsupportedOperationException("Push notifications are not available on this platform yet."))
    }
}

expect fun createPushNotificationTokenProvider(): PushNotificationTokenProvider

expect fun pushNotificationPlatform(): String
