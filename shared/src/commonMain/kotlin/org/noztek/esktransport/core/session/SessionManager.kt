package org.noztek.esktransport.core.session

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SessionManager(
    private val settings: Settings,
    private val tokenCache: TokenCache = TokenCache(),
) {
    private val rolesJson = Json { ignoreUnknownKeys = true }

    private val _userId = MutableStateFlow(settings.getLongOrNull(SessionKeys.USER_ID))
    private val _accessToken = MutableStateFlow(settings.getStringOrNull(SessionKeys.ACCESS_TOKEN))
    private val _userRole = MutableStateFlow(settings.getStringOrNull(SessionKeys.USER_ROLE))
    private val _userRoles = MutableStateFlow(readRoles())
    private val _userName = MutableStateFlow(settings.getStringOrNull(SessionKeys.USER_NAME))
    private val _userPhone = MutableStateFlow(settings.getStringOrNull(SessionKeys.USER_PHONE))

    val userId: StateFlow<Long?> = _userId.asStateFlow()
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    val userRoles: StateFlow<Set<String>> = _userRoles.asStateFlow()
    val userName: StateFlow<String?> = _userName.asStateFlow()
    val userPhone: StateFlow<String?> = _userPhone.asStateFlow()
    val isLoggedIn = accessToken.map { !it.isNullOrBlank() }
    fun hasSeenStarter(): Boolean = settings.getBoolean(SessionKeys.HAS_SEEN_STARTER, false)

    init {
        tokenCache.set(_accessToken.value)
    }

    fun saveSession(
        userId: Long?,
        token: String,
        roles: List<String>,
        name: String?,
        phone: String?,
        expiresAtMs: Long?,
    ) {
        val normalizedRoles = roles
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toSet()
        val primaryRole = normalizedRoles.firstOrNull()

        tokenCache.set(token)

        if (userId != null) settings.putLong(SessionKeys.USER_ID, userId) else settings.remove(SessionKeys.USER_ID)
        settings.putString(SessionKeys.ACCESS_TOKEN, token)

        if (normalizedRoles.isNotEmpty()) {
            settings.putString(SessionKeys.USER_ROLES_JSON, rolesJson.encodeToString(normalizedRoles.toList()))
        } else {
            settings.remove(SessionKeys.USER_ROLES_JSON)
        }

        if (primaryRole != null) settings.putString(SessionKeys.USER_ROLE, primaryRole) else settings.remove(SessionKeys.USER_ROLE)
        if (name != null) settings.putString(SessionKeys.USER_NAME, name) else settings.remove(SessionKeys.USER_NAME)
        if (phone != null) settings.putString(SessionKeys.USER_PHONE, phone) else settings.remove(SessionKeys.USER_PHONE)
        if (expiresAtMs != null) settings.putLong(SessionKeys.EXPIRES_AT, expiresAtMs) else settings.remove(SessionKeys.EXPIRES_AT)

        _userId.value = userId
        _accessToken.value = token
        _userRole.value = primaryRole
        _userRoles.value = normalizedRoles
        _userName.value = name
        _userPhone.value = phone
    }

    fun clear() {
        tokenCache.clear()
        settings.clear()

        _userId.value = null
        _accessToken.value = null
        _userRole.value = null
        _userRoles.value = emptySet()
        _userName.value = null
        _userPhone.value = null
    }

    fun markStarterSeen() {
        settings.putBoolean(SessionKeys.HAS_SEEN_STARTER, true)
    }

    fun cachedToken(): String? = tokenCache.get()

    private fun readRoles(): Set<String> {
        val raw = settings.getStringOrNull(SessionKeys.USER_ROLES_JSON) ?: return emptySet()
        return runCatching { rolesJson.decodeFromString<List<String>>(raw).map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet() }
            .getOrDefault(emptySet())
    }

    class TokenCache {
        private var token: String? = null

        fun set(value: String?) {
            token = value
        }

        fun get(): String? = token

        fun clear() {
            token = null
        }
    }
}

private object SessionKeys {
    const val USER_ID = "session.user_id"
    const val ACCESS_TOKEN = "session.access_token"
    const val USER_ROLE = "session.user_role"
    const val USER_ROLES_JSON = "session.user_roles_json"
    const val USER_NAME = "session.user_name"
    const val USER_PHONE = "session.user_phone"
    const val EXPIRES_AT = "session.expires_at"
    const val HAS_SEEN_STARTER = "session.has_seen_starter"
}
