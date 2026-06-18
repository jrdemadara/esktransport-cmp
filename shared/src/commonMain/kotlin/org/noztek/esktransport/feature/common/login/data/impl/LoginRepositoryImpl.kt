package org.noztek.esktransport.feature.common.login.data.impl

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
            val message = parseLoginError(throwable)
            Result.failure(IllegalStateException(message))
        }
    }
}

private suspend fun parseLoginError(throwable: Throwable): String {
    if (throwable is ResponseException) {
        if (throwable.response.status == HttpStatusCode.Unauthorized) {
            return "Invalid phone number or password."
        }

        val body = runCatching { throwable.response.bodyAsText() }.getOrNull()
        val parsed = body?.firstLaravelErrorMessage()
        if (!parsed.isNullOrBlank()) return parsed
    }

    return ApiErrorParser.parse(
        throwable = throwable,
        fallback = "Unable to log in. Please try again.",
    )
}

private fun String.firstLaravelErrorMessage(): String? {
    val root = runCatching { Json.parseToJsonElement(this).jsonObject }.getOrNull() ?: return null
    val errors = root["errors"] as? JsonObject
    val firstFieldError = errors
        ?.values
        ?.firstOrNull()
        ?.let { value ->
            when (value) {
                is JsonArray -> value.firstOrNull()?.jsonPrimitive?.content
                else -> value.jsonPrimitive.content
            }
        }

    return firstFieldError ?: root["message"]?.jsonPrimitive?.content
}
