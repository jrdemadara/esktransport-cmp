package org.noztek.esktransport.feature.driver.onboarding.presentation

import androidx.compose.runtime.Composable
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType

data class CapturedDocumentImage(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CapturedDocumentImage) return false

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

@Composable
expect fun DriverDocumentCaptureScreen(
    type: DriverOnboardingDocumentType,
    onCaptured: (CapturedDocumentImage) -> Unit,
    onClose: () -> Unit,
)
