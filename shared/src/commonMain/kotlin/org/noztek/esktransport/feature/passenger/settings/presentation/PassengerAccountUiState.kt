package org.noztek.esktransport.feature.passenger.settings.presentation

data class PassengerAccountUiState(
    val id: Long? = null,
    val publicId: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val profilePhotoBytes: ByteArray? = null,
    val status: String = "",
    val isLoading: Boolean = false,
    val isSavingAccount: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val activeEditField: PassengerAccountEditableField? = null,
    val editValue: String = "",
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PassengerAccountUiState) return false

        return id == other.id &&
            publicId == other.publicId &&
            name == other.name &&
            phone == other.phone &&
            email == other.email &&
            address == other.address &&
            profilePhotoBytes.contentEquals(other.profilePhotoBytes) &&
            status == other.status &&
            isLoading == other.isLoading &&
            isSavingAccount == other.isSavingAccount &&
            isUploadingPhoto == other.isUploadingPhoto &&
            isLoggingOut == other.isLoggingOut &&
            isLoggedOut == other.isLoggedOut &&
            activeEditField == other.activeEditField &&
            editValue == other.editValue &&
            errorMessage == other.errorMessage &&
            statusMessage == other.statusMessage
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (publicId?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + (profilePhotoBytes?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isSavingAccount.hashCode()
        result = 31 * result + isUploadingPhoto.hashCode()
        result = 31 * result + isLoggingOut.hashCode()
        result = 31 * result + isLoggedOut.hashCode()
        result = 31 * result + (activeEditField?.hashCode() ?: 0)
        result = 31 * result + editValue.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (statusMessage?.hashCode() ?: 0)
        return result
    }
}

enum class PassengerAccountEditableField {
    Email,
    Address,
}
