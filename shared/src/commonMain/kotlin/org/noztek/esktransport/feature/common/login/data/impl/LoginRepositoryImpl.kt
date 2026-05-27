package org.noztek.esktransport.feature.common.login.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.login.data.remote.LoginApi
import org.noztek.esktransport.feature.common.login.data.remote.dto.LoginRequestDto
import org.noztek.esktransport.feature.common.login.domain.model.LoginPayload
import org.noztek.esktransport.feature.common.login.domain.repository.LoginRepository
import kotlin.time.Instant

class LoginRepositoryImpl(
    private val loginApi: LoginApi,
    private val sessionManager: SessionManager,
) : LoginRepository {
    override suspend fun login(payload: LoginPayload): Result<Unit> {
        return try {
            val response = loginApi.login(
                LoginRequestDto(
                    phone = payload.phone,
                    password = payload.password,
                    deviceName = payload.deviceName
                )
            )
            val expiresAtMs = response.expiresAt
                ?.let { Instant.parse(it).toEpochMilliseconds() }

            sessionManager.saveSession(
                userId = response.data.id.toLong(),
                token = response.accessToken,
                roles = response.data.roles,
                name = response.data.name,
                phone = response.data.phone,
                expiresAtMs = expiresAtMs
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(
                throwable = throwable,
                fallback = "Login failed."
            )
            Result.failure(IllegalStateException(message))
        }
    }
}
