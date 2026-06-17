package org.noztek.esktransport.core.platform

import androidx.compose.runtime.Composable

data class PickedPlatformFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedPlatformFile) return false

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

class PlatformFilePicker internal constructor(
    val isSupported: Boolean,
    private val launchPicker: (Array<String>) -> Unit,
) {
    fun launch(mimeTypes: Array<String>) {
        launchPicker(mimeTypes)
    }
}

@Composable
expect fun rememberPlatformFilePicker(
    onFilePicked: (PickedPlatformFile) -> Unit,
): PlatformFilePicker
