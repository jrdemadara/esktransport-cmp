package org.noztek.esktransport.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformFilePicker(
    onFilePicked: (PickedPlatformFile) -> Unit,
): PlatformFilePicker {
    return remember {
        PlatformFilePicker(isSupported = false) {
            // iOS document picking needs a UIKit bridge; keep the flow compiling until that is wired.
        }
    }
}
