package org.noztek.esktransport.feature.common.otp.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class OtpStateStore(
    private val settings: Settings,
) {
    private val _pendingPhone = MutableStateFlow(settings.getStringOrNull(KEY_PENDING_PHONE))
    private val _pendingPurpose = MutableStateFlow(settings.getStringOrNull(KEY_PENDING_PURPOSE))

    val pendingPhone: Flow<String?> = _pendingPhone
    val pendingPurpose: Flow<String?> = _pendingPurpose

    suspend fun setPendingPhone(phone: String) {
        settings.putString(KEY_PENDING_PHONE, phone)
        _pendingPhone.value = phone
    }

    suspend fun setPendingPurpose(purpose: String) {
        settings.putString(KEY_PENDING_PURPOSE, purpose)
        _pendingPurpose.value = purpose
    }

    suspend fun clearPendingState() {
        settings.remove(KEY_PENDING_PHONE)
        settings.remove(KEY_PENDING_PURPOSE)
        _pendingPhone.value = null
        _pendingPurpose.value = null
    }

    suspend fun clearPendingPhone() {
        settings.remove(KEY_PENDING_PHONE)
        _pendingPhone.value = null
    }

    suspend fun getResendAvailableAtMs(): Long? = settings.getLongOrNull(KEY_RESEND_AVAILABLE_AT_MS)

    suspend fun setResendAvailableAtMs(value: Long) {
        settings.putLong(KEY_RESEND_AVAILABLE_AT_MS, value)
    }

    suspend fun clearResendAvailableAtMs() {
        settings.remove(KEY_RESEND_AVAILABLE_AT_MS)
    }

    private companion object {
        const val KEY_PENDING_PHONE = "otp_pending_phone"
        const val KEY_PENDING_PURPOSE = "otp_pending_purpose"
        const val KEY_RESEND_AVAILABLE_AT_MS = "otp_resend_available_at_ms"
    }
}
