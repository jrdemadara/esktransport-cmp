package org.noztek.esktransport.feature.driver.settings.presentation

data class DriverSettingsUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val role: String = "Driver",
    val driverId: Long? = null,
    val isVerifiedDriver: Boolean = false,
    val profilePhotoBytes: ByteArray? = null,
    val activeEditField: DriverAccountEditableField? = null,
    val editValue: String = "",
    val isSavingAccount: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DriverSettingsUiState) return false

        return name == other.name &&
            phone == other.phone &&
            email == other.email &&
            address == other.address &&
            role == other.role &&
            driverId == other.driverId &&
            isVerifiedDriver == other.isVerifiedDriver &&
            profilePhotoBytes.contentEquals(other.profilePhotoBytes) &&
            activeEditField == other.activeEditField &&
            editValue == other.editValue &&
            isSavingAccount == other.isSavingAccount &&
            isLoggingOut == other.isLoggingOut &&
            isLoggedOut == other.isLoggedOut &&
            errorMessage == other.errorMessage &&
            statusMessage == other.statusMessage
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + role.hashCode()
        result = 31 * result + (driverId?.hashCode() ?: 0)
        result = 31 * result + isVerifiedDriver.hashCode()
        result = 31 * result + (profilePhotoBytes?.contentHashCode() ?: 0)
        result = 31 * result + (activeEditField?.hashCode() ?: 0)
        result = 31 * result + editValue.hashCode()
        result = 31 * result + isSavingAccount.hashCode()
        result = 31 * result + isLoggingOut.hashCode()
        result = 31 * result + isLoggedOut.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (statusMessage?.hashCode() ?: 0)
        return result
    }
}

enum class DriverAccountEditableField {
    Email,
    Address,
}
