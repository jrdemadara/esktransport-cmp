package org.noztek.esktransport.app.di.modules

import org.koin.dsl.module
import org.noztek.esktransport.core.map.MapCameraDefaults

val mapModule = module {
    single { MapCameraDefaults() }
    single { org.noztek.esktransport.core.map.MapboxDirectionsClient(get(), get()) }
}
