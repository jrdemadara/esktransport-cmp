package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.driver.home.data.impl.DriverHomeRepositoryImpl
import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeApi
import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeRepository
import org.noztek.esktransport.feature.driver.home.domain.lifecycle.DriverAvailabilityLifecycleCoordinator
import org.noztek.esktransport.feature.driver.home.domain.usecase.AcceptDriverHomeOfferUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.GetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.home.domain.usecase.SetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.home.presentation.DriverHomeViewModel

val driverHomeModule = module {
    single { DriverHomeApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverHomeRepository> { DriverHomeRepositoryImpl(api = get()) }
    single { GetDriverAvailabilityUseCase(repository = get()) }
    single { SetDriverAvailabilityUseCase(repository = get()) }
    single { AcceptDriverHomeOfferUseCase(api = get()) }
    single {
        DriverAvailabilityLifecycleCoordinator(
            sessionManager = get(),
            setDriverAvailabilityUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverHomeViewModel(
            getDriverAvailabilityUseCase = get(),
            setDriverAvailabilityUseCase = get(),
            acceptDriverHomeOfferUseCase = get(),
            realtimeCoordinator = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
