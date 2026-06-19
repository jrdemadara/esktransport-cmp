package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.driver.go.data.impl.GoRepositoryImpl
import org.noztek.esktransport.feature.driver.go.data.remote.GoApi
import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository
import org.noztek.esktransport.feature.driver.go.domain.lifecycle.DriverAvailabilityLifecycleCoordinator
import org.noztek.esktransport.feature.driver.go.domain.usecase.AcceptOfferUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.ExpireOfferUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.GetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.go.domain.usecase.SetDriverAvailabilityUseCase
import org.noztek.esktransport.feature.driver.go.presentation.GoViewModel
import org.noztek.esktransport.feature.driver.home.presentation.HomeViewModel
import org.noztek.esktransport.feature.driver.onboarding.data.impl.DriverOnboardingRepositoryImpl
import org.noztek.esktransport.feature.driver.onboarding.data.remote.DriverOnboardingApi
import org.noztek.esktransport.feature.driver.onboarding.domain.repository.DriverOnboardingRepository
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverServiceZonesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.ObserveDriverOnboardingStatusChangedUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverIdentityVerificationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverServiceZonesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverVehicleRegistrationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UnsubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverOnboardingViewModel

val driverHomeModule = module {
    single { GoApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<GoRepository> { GoRepositoryImpl(api = get()) }
    single { DriverOnboardingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverOnboardingRepository> { DriverOnboardingRepositoryImpl(api = get()) }
    single { GetDriverAvailabilityUseCase(repository = get()) }
    single { SetDriverAvailabilityUseCase(repository = get()) }
    single { GetDriverOnboardingStatusUseCase(repository = get()) }
    single { GetDriverServiceZonesUseCase(repository = get()) }
    single { ObserveDriverOnboardingStatusChangedUseCase(realtime = get()) }
    single { SubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { UnsubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { SubmitDriverIdentityVerificationUseCase(repository = get()) }
    single { SubmitDriverVehicleRegistrationUseCase(repository = get()) }
    single { SubmitDriverServiceZonesUseCase(repository = get()) }
    single { AcceptOfferUseCase(api = get()) }
    single { ExpireOfferUseCase(api = get()) }
    single {
        DriverAvailabilityLifecycleCoordinator(
            sessionManager = get(),
            setDriverAvailabilityUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        HomeViewModel(
            getDriverOnboardingStatusUseCase = get(),
            observeDriverOnboardingStatusChangedUseCase = get(),
            subscribeDriverOnboardingRealtimeUseCase = get(),
            unsubscribeDriverOnboardingRealtimeUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverOnboardingViewModel(
            getDriverOnboardingStatusUseCase = get(),
            getDriverServiceZonesUseCase = get(),
            submitDriverIdentityVerificationUseCase = get(),
            submitDriverVehicleRegistrationUseCase = get(),
            submitDriverServiceZonesUseCase = get(),
            observeDriverOnboardingStatusChangedUseCase = get(),
            subscribeDriverOnboardingRealtimeUseCase = get(),
            unsubscribeDriverOnboardingRealtimeUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        GoViewModel(
            getDriverAvailabilityUseCase = get(),
            setDriverAvailabilityUseCase = get(),
            acceptOfferUseCase = get(),
            expireOfferUseCase = get(),
            getActiveBookingUseCase = get(),
            realtimeCoordinator = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
