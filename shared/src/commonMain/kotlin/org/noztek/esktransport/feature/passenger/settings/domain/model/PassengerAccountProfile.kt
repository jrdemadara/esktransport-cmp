package org.noztek.esktransport.feature.passenger.settings.domain.model

data class PassengerAccountProfile(
    val id: Long?,
    val publicId: String?,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val hasProfilePhoto: Boolean,
    val status: String,
)

data class PassengerProfilePhotoUpload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PassengerProfilePhotoUpload) return false

        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
