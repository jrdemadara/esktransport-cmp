package org.noztek.esktransport.feature.common.otp.domain.usecase

import org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository

class RequestOtpUseCase(
    private val repository: org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository,
) {
    suspend operator fun invoke(phone: String, purpose: String): Result<Unit> =
        repository.requestOtp(phone = phone, purpose = purpose)
}
