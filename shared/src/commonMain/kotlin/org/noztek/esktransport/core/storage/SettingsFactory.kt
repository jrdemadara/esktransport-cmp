package org.noztek.esktransport.core.storage

import com.russhwolf.settings.Settings
import org.noztek.esktransport.app.di.PlatformKoinContext

expect fun createPlatformSettings(context: PlatformKoinContext): Settings
