package org.noztek.esktransport.core.storage

import com.russhwolf.settings.Settings
import org.noztek.esktransport.app.di.PlatformKoinContext

actual fun createPlatformSettings(context: PlatformKoinContext): Settings = Settings()
