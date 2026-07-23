package org.noztek.esktransport.feature.driver.settings.presentation

data class DriverSettingsUiState(
    val name: String = "",
    val phone: String = "",
    val role: String = "Driver",
    val driverId: Long? = null,
    val profilePhotoBytes: ByteArray? = null,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DriverSettingsUiState) return false

        return name == other.name &&
            phone == other.phone &&
            role == other.role &&
            driverId == other.driverId &&
            profilePhotoBytes.contentEquals(other.profilePhotoBytes) &&
            isLoggingOut == other.isLoggingOut &&
            isLoggedOut == other.isLoggedOut &&
            errorMessage == other.errorMessage
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + (driverId?.hashCode() ?: 0)
        result = 31 * result + (profilePhotoBytes?.contentHashCode() ?: 0)
        result = 31 * result + isLoggingOut.hashCode()
        result = 31 * result + isLoggedOut.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
