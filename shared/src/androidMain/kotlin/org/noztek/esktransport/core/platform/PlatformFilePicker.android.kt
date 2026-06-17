package org.noztek.esktransport.core.platform

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformFilePicker(
    onFilePicked: (PickedPlatformFile) -> Unit,
): PlatformFilePicker {
    val context = LocalContext.current
    val latestOnFilePicked = rememberUpdatedState(onFilePicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: "driver-document"

        val bytes = resolver.openInputStream(uri)?.use { input -> input.readBytes() } ?: return@rememberLauncherForActivityResult
        latestOnFilePicked.value(
            PickedPlatformFile(
                fileName = fileName,
                mimeType = mimeType,
                bytes = bytes,
            ),
        )
    }

    return remember(launcher) {
        PlatformFilePicker(isSupported = true) { mimeTypes ->
            launcher.launch(mimeTypes)
        }
    }
}
