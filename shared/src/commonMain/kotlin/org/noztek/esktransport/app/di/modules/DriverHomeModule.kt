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
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverVehicleServicesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UnsubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverOnboardingViewModel
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverEmergencyContactRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverIncidentReportRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverLocationSharingRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverMarketplaceListingRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverSettingsRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.impl.DriverVehicleRepositoryImpl
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverEmergencyContactApi
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverIncidentReportApi
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverLocationSharingApi
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverMarketplaceListingApi
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverSettingsApi
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverVehicleApi
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverEmergencyContactRepository
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverIncidentReportRepository
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverLocationSharingRepository
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverMarketplaceListingRepository
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverSettingsRepository
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverVehicleRepository
import org.noztek.esktransport.feature.driver.settings.domain.usecase.ActivateDriverRideVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.AddDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.DeleteDriverEmergencyContactUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverAccountUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverEmergencyContactsUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverIncidentReportsUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverLocationSharingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverMarketplaceListingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverProfilePhotoUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehiclePhotoUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehicleTypesUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehiclesUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.SaveDriverEmergencyContactUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.SubmitDriverIncidentReportUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverLocationSharingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverAccountUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverMarketplaceListingUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UploadDriverVehicleDocumentUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverVehicleServicesUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.presentation.DriverSettingsViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverEmergencyContactsViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverIncidentReportViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverLocationSharingViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.MarketplaceListingEditViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverServiceAreasViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverVerificationViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverVehicleDetailViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverVehicleFormViewModel
import org.noztek.esktransport.feature.driver.settings.presentation.DriverVehiclesViewModel
import org.noztek.esktransport.feature.driver.session.presentation.DriverSessionViewModel
import org.noztek.esktransport.feature.driver.trips.data.impl.DriverTripsRepositoryImpl
import org.noztek.esktransport.feature.driver.trips.data.remote.DriverTripsApi
import org.noztek.esktransport.feature.driver.trips.domain.repository.DriverTripsRepository
import org.noztek.esktransport.feature.driver.trips.domain.usecase.GetDriverTripsUseCase
import org.noztek.esktransport.feature.driver.trips.presentation.TripsViewModel
import org.noztek.esktransport.feature.driver.wallet.data.impl.DriverWalletRepositoryImpl
import org.noztek.esktransport.feature.driver.wallet.data.remote.DriverWalletApi
import org.noztek.esktransport.feature.driver.wallet.domain.repository.DriverWalletRepository
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CancelDriverCashoutUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CancelDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverCashoutUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase
import org.noztek.esktransport.feature.driver.wallet.presentation.TransactionHistoryViewModel
import org.noztek.esktransport.feature.common.cashout.presentation.CashoutViewModel
import org.noztek.esktransport.feature.common.topup.presentation.TopUpViewModel

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
    single { DriverTripsApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverTripsRepository> { DriverTripsRepositoryImpl(api = get()) }
    single { DriverSettingsApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverSettingsRepository> { DriverSettingsRepositoryImpl(api = get()) }
    single { DriverVehicleApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverVehicleRepository> { DriverVehicleRepositoryImpl(api = get()) }
    single { DriverMarketplaceListingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverMarketplaceListingRepository> { DriverMarketplaceListingRepositoryImpl(api = get()) }
    single { DriverEmergencyContactApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverEmergencyContactRepository> { DriverEmergencyContactRepositoryImpl(api = get()) }
    single { DriverIncidentReportApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverIncidentReportRepository> { DriverIncidentReportRepositoryImpl(api = get()) }
    single { DriverLocationSharingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<DriverLocationSharingRepository> { DriverLocationSharingRepositoryImpl(api = get()) }
    single { GetDriverAvailabilityUseCase(repository = get()) }
    single { SetDriverAvailabilityUseCase(repository = get()) }
    single { GetDriverHomeStatsUseCase(repository = get()) }
    single { GetDriverOnboardingStatusUseCase(repository = get()) }
    single { GetDriverServiceZonesUseCase(repository = get()) }
    single { GetDriverWalletUseCase(repository = get()) }
    single { CreateDriverTopupUseCase(repository = get()) }
    single { CancelDriverTopupUseCase(repository = get()) }
    single { CreateDriverCashoutUseCase(repository = get()) }
    single { CancelDriverCashoutUseCase(repository = get()) }
    single { GetRiderEarningsUseCase(repository = get()) }
    single { GetDriverTripsUseCase(repository = get()) }
    single { GetDriverAccountUseCase(repository = get()) }
    single { UpdateDriverAccountUseCase(repository = get()) }
    single { GetDriverProfilePhotoUseCase(repository = get()) }
    single { GetDriverEmergencyContactsUseCase(repository = get()) }
    single { SaveDriverEmergencyContactUseCase(repository = get()) }
    single { DeleteDriverEmergencyContactUseCase(repository = get()) }
    single { GetDriverIncidentReportsUseCase(repository = get()) }
    single { SubmitDriverIncidentReportUseCase(repository = get()) }
    single { GetDriverLocationSharingUseCase(repository = get()) }
    single { UpdateDriverLocationSharingUseCase(repository = get()) }
    single { GetDriverVehiclesUseCase(repository = get()) }
    single { GetDriverVehiclePhotoUseCase(repository = get()) }
    single { GetDriverVehicleUseCase(repository = get()) }
    single { GetDriverVehicleTypesUseCase(repository = get()) }
    single { AddDriverVehicleUseCase(repository = get()) }
    single { UpdateDriverVehicleUseCase(repository = get()) }
    single { UpdateDriverVehicleServicesUseCase(repository = get()) }
    single { UploadDriverVehicleDocumentUseCase(repository = get()) }
    single { ActivateDriverRideVehicleUseCase(repository = get()) }
    single { GetDriverMarketplaceListingUseCase(repository = get()) }
    single { UpdateDriverMarketplaceListingUseCase(repository = get()) }
    single { ObserveDriverOnboardingStatusChangedUseCase(realtime = get()) }
    single { SubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { UnsubscribeDriverOnboardingRealtimeUseCase(realtime = get()) }
    single { SubmitDriverIdentityVerificationUseCase(repository = get()) }
    single { SubmitDriverVehicleRegistrationUseCase(repository = get()) }
    single { SubmitDriverVehicleServicesUseCase(repository = get()) }
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
            getDriverTripsUseCase = get(),
            getDriverProfilePhotoUseCase = get(),
            mapboxDirectionsClient = get(),
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
            submitDriverVehicleServicesUseCase = get(),
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
            getDriverWalletUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        TripsViewModel(
            getDriverTripsUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverSettingsViewModel(
            observeCurrentSessionUseCase = get(),
            logoutUseCase = get(),
            getDriverAccountUseCase = get(),
            updateDriverAccountUseCase = get(),
            getDriverProfilePhotoUseCase = get(),
            getDriverOnboardingStatusUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverVehiclesViewModel(
            getDriverVehiclesUseCase = get(),
            getDriverVehiclePhotoUseCase = get(),
            activateDriverRideVehicleUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverServiceAreasViewModel(
            getDriverOnboardingStatusUseCase = get(),
            getDriverServiceZonesUseCase = get(),
            submitDriverServiceZonesUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverVerificationViewModel(
            getDriverOnboardingStatusUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverEmergencyContactsViewModel(
            getDriverEmergencyContactsUseCase = get(),
            saveDriverEmergencyContactUseCase = get(),
            deleteDriverEmergencyContactUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverIncidentReportViewModel(
            getDriverIncidentReportsUseCase = get(),
            submitDriverIncidentReportUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverLocationSharingViewModel(
            getDriverLocationSharingUseCase = get(),
            updateDriverLocationSharingUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverVehicleFormViewModel(
            getDriverVehicleTypesUseCase = get(),
            getDriverVehicleUseCase = get(),
            addDriverVehicleUseCase = get(),
            updateDriverVehicleUseCase = get(),
            uploadDriverVehicleDocumentUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        DriverVehicleDetailViewModel(
            getDriverVehicleUseCase = get(),
            uploadDriverVehicleDocumentUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        MarketplaceListingEditViewModel(
            getDriverMarketplaceListingUseCase = get(),
            updateDriverMarketplaceListingUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        TransactionHistoryViewModel(
            getDriverWalletUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        TopUpViewModel(
            getDriverWalletUseCase = get(),
            createDriverTopupUseCase = get(),
            cancelDriverTopupUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        CashoutViewModel(
            getDriverWalletUseCase = get(),
            createDriverCashoutUseCase = get(),
            cancelDriverCashoutUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
