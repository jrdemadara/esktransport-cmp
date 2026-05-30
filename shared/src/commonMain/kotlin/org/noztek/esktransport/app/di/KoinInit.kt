package org.noztek.esktransport.app.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.noztek.esktransport.app.di.modules.coreModule
import org.noztek.esktransport.app.di.modules.driverHomeModule
import org.noztek.esktransport.app.di.modules.driverTripNavigationModule
import org.noztek.esktransport.app.di.modules.forgotPasswordModule
import org.noztek.esktransport.app.di.modules.loginModule
import org.noztek.esktransport.app.di.modules.networkModule
import org.noztek.esktransport.app.di.modules.logoutModule
import org.noztek.esktransport.app.di.modules.mapModule
import org.noztek.esktransport.app.di.modules.otpModule
import org.noztek.esktransport.app.di.modules.passengerModule
import org.noztek.esktransport.app.di.modules.registerModule
import org.noztek.esktransport.app.di.modules.resetPasswordModule
import org.noztek.esktransport.app.di.modules.sessionModule

private val appModules = listOf(
    networkModule,
    coreModule,
    mapModule,
    sessionModule,
    loginModule,
    registerModule,
    otpModule,
    forgotPasswordModule,
    resetPasswordModule,
    logoutModule,
    driverHomeModule,
    driverTripNavigationModule,
    passengerModule,
)

fun initKoin(
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(appModules + extraModules)
    }
}
