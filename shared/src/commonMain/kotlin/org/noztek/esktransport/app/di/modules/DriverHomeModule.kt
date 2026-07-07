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
import org.noztek.esktransport.feature.driver.earning.data.impl.RiderEarningsRepositoryImpl
import org.noztek.esktransport.feature.driver.earning.data.remote.RiderEarningsApi
import org.noztek.esktransport.feature.driver.earning.domain.repository.RiderEarningsRepository
import org.noztek.esktransport.feature.driver.earning.domain.usecase.GetRiderEarningsUseCase
import org.noztek.esktransport.feature.driver.earning.presentation.EarningsViewModel
import org.noztek.esktransport.feature.driver.home.data.impl.DriverHomeStatsRepositoryImpl
import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeStatsApi
import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeStatsRepository
import org.noztek.esktransport.feature.driver.home.domain.usecase.GetDriverHomeStatsUseCase
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
import org.noztek.esktransport.feature.driver.session.presentation.DriverSessionViewModel
import org.noztek.esktransport.feature.driver.wallet.data.impl.DriverWalletRepositoryImpl
import org.noztek.esktransport.feature.driver.wallet.data.remote.DriverWalletApi
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

val driverHomeModule = module {
    single { GoApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<GoRepository> { GoRepositoryImpl(api = get()) }
    single { DriverHomeStatsApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverHomeStatsRepository> { DriverHomeStatsRepositoryImpl(api = get()) }
    single { DriverOnboardingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverOnboardingRepository> { DriverOnboardingRepositoryImpl(api = get()) }
    single { DriverWalletApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverWalletRepository> { DriverWalletRepositoryImpl(api = get()) }
    single { RiderEarningsApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<RiderEarningsRepository> { RiderEarningsRepositoryImpl(api = get()) }
    single { GetDriverAvailabilityUseCase(repository = get()) }
    single { SetDriverAvailabilityUseCase(repository = get()) }
    single { GetDriverHomeStatsUseCase(repository = get()) }
    single { GetDriverOnboardingStatusUseCase(repository = get()) }
    single { GetDriverServiceZonesUseCase(repository = get()) }
    single { GetDriverWalletUseCase(repository = get()) }
    single { CreateDriverTopupUseCase(repository = get()) }
    single { GetRiderEarningsUseCase(repository = get()) }
    single { ObserveDriverOnboardingStatusChangedUseCase(realtime = get()) }
    single { SubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { UnsubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { SubmitDriverIdentityVerificationUseCase(repository = get()) }
    single { SubmitDriverVehicleRegistrationUseCase(repository = get()) }
    single { SubmitDriverServiceZonesUseCase(repository = get()) }
    single { AcceptOfferUseCase(api = get(), currentLocationProvider = get()) }
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
            observeCurrentSessionUseCase = get(),
            getDriverOnboardingStatusUseCase = get(),
            observeDriverOnboardingStatusChangedUseCase = get(),
            subscribeDriverOnboardingRealtimeUseCase = get(),
            unsubscribeDriverOnboardingRealtimeUseCase = get(),
            getDriverHomeStatsUseCase = get(),
            getDriverWalletUseCase = get(),
            getRiderEarningsUseCase = get(),
            createDriverTopupUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverSessionViewModel(
            getDriverActiveBookingUseCase = get(),
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
            availabilityLifecycleCoordinator = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        EarningsViewModel(
            getRiderEarningsUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
