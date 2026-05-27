package org.noztek.esktransport.feature.common.otp.domain.repository

interface OtpRepository {
    suspend fun requestOtp(phone: String, purpose: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otpCode: String, purpose: String): Result<String?>
}
