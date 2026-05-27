package org.noztek.esktransport.feature.common.otp.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.otp.data.remote.OtpApi
import org.noztek.esktransport.feature.common.otp.data.remote.dto.RequestOtpRequestDto
import org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpRequestDto
import org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository

class OtpRepositoryImpl(
    private val otpApi: org.noztek.esktransport.feature.common.otp.data.remote.OtpApi,
) : org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository {
    override suspend fun requestOtp(phone: String, purpose: String): Result<Unit> {
        return try {
            otpApi.requestOtp(
               RequestOtpRequestDto(
                    phone = phone,
                    purpose = purpose
                ),
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Resend OTP failed.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun verifyOtp(phone: String, otpCode: String, purpose: String): Result<String?> {
        return try {
            val response = otpApi.verifyOtp(
                VerifyOtpRequestDto(
                    phone = phone,
                    otpCode = otpCode,
                    purpose = purpose
                ),
            )
            Result.success(response.data?.resetToken)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "OTP verification failed.")
            Result.failure(IllegalStateException(message))
        }
    }
}
