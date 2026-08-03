package org.noztek.esktransport.core.notify

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

actual fun createPushNotificationTokenProvider(): PushNotificationTokenProvider {
    return FirebasePushNotificationTokenProvider(
        messaging = FirebaseMessaging.getInstance(),
    )
}

actual fun pushNotificationPlatform(): String = "android"

private class FirebasePushNotificationTokenProvider(
    private val messaging: FirebaseMessaging,
) : PushNotificationTokenProvider {
    override suspend fun currentToken(): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val task = messaging.token
                .addOnSuccessListener { token ->
                    continuation.resume(Result.success(token))
                }
                .addOnFailureListener { throwable ->
                    continuation.resume(Result.failure(throwable))
                }
        }
    }
}
