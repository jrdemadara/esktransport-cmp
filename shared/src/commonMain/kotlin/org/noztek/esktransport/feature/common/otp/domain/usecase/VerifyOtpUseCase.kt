package org.noztek.esktransport.feature.common.otp.domain.usecase

import org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository

class VerifyOtpUseCase(
    private val repository: org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository,
) {
    suspend operator fun invoke(phone: String, otpCode: String, purpose: String): Result<String?> =
        repository.verifyOtp(phone = phone, otpCode = otpCode, purpose = purpose)
}
