package org.noztek.esktransport.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.noztek.esktransport.app.di.PlatformKoinContext

actual fun createPlatformSettings(context: PlatformKoinContext): Settings {
    val sharedPrefs = context.appContext.getSharedPreferences("esktransport.settings", android.content.Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPrefs)
}
