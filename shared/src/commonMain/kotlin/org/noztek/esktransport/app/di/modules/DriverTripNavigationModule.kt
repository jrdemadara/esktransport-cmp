package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.rider.trip_navigation.data.impl.RiderTripNavigationRepositoryImpl
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.RiderTripNavigationApi
import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.GetRiderTripSessionUseCase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase.UpdateRiderTripLocationUseCase
import org.noztek.esktransport.feature.driver.trip_navigation.presentation.TripNavigationViewModel

val driverTripNavigationModule = module {
    single {
        RiderTripNavigationApi(
            client = get(),
            baseUrl = get(named(API_BASE_URL_QUALIFIER)),
        )
    }
    single<RiderTripNavigationRepository> { RiderTripNavigationRepositoryImpl(api = get()) }
    single { GetRiderTripSessionUseCase(repository = get()) }
    single { UpdateRiderTripLocationUseCase(repository = get()) }
    factory {
        TripNavigationViewModel(
            getRiderTripSessionUseCase = get(),
            updateRiderTripLocationUseCase = get(),
            mapboxDirectionsClient = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
