package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.passenger.booking_review.data.impl.BookingReviewRepositoryImpl
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.BookingReviewApi
import org.noztek.esktransport.feature.passenger.booking_review.domain.repository.BookingReviewRepository
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CancelBookingUseCase
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateBookingUseCase
import org.noztek.esktransport.feature.passenger.booking_review.domain.usecase.CreateFareQuoteUseCase
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewViewModel
import org.noztek.esktransport.feature.passenger.activity.data.impl.PassengerActivityRepositoryImpl
import org.noztek.esktransport.feature.passenger.activity.data.remote.PassengerActivityApi
import org.noztek.esktransport.feature.passenger.activity.domain.repository.PassengerActivityRepository
import org.noztek.esktransport.feature.passenger.activity.domain.usecase.GetPassengerActivityUseCase
import org.noztek.esktransport.feature.passenger.activity.presentation.ActivityViewModel
import org.noztek.esktransport.feature.passenger.home.data.impl.KnownPlacesRepositoryImpl
import org.noztek.esktransport.feature.passenger.home.data.remote.KnownPlacesApi
import org.noztek.esktransport.feature.passenger.home.domain.repository.KnownPlacesRepository
import org.noztek.esktransport.feature.passenger.home.domain.usecase.GetKnownPlacesUseCase
import org.noztek.esktransport.feature.passenger.home.presentation.PassengerHomeViewModel
import org.noztek.esktransport.feature.passenger.kudi.data.impl.KudiRepositoryImpl
import org.noztek.esktransport.feature.passenger.kudi.data.remote.KudiApi
import org.noztek.esktransport.feature.passenger.kudi.domain.repository.KudiRepository
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.CreateKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.GetCurrentKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.SendKudiMessageUseCase
import org.noztek.esktransport.feature.passenger.kudi.presentation.KudiViewModel
import org.noztek.esktransport.feature.passenger.marketplace.data.impl.MarketplaceRepositoryImpl
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.MarketplaceApi
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetRentalListingsUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetRentalVehicleTypesUseCase
import org.noztek.esktransport.feature.passenger.marketplace.presentation.MarketplaceListingDetailsViewModel
import org.noztek.esktransport.feature.passenger.marketplace.presentation.MarketplaceViewModel
import org.noztek.esktransport.feature.passenger.location_search.data.impl.LocationRepositoryImpl
import org.noztek.esktransport.feature.passenger.location_search.data.impl.PlaceSearchRepositoryImpl
import org.noztek.esktransport.feature.passenger.location_search.data.impl.ReverseGeocodeRepositoryImpl
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.LocationRepository
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.PlaceSearchRepository
import org.noztek.esktransport.feature.passenger.location_search.domain.repository.ReverseGeocodeRepository
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.GetCurrentLocationUseCase
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.ResolveTapLabelUseCase
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.SearchPlacesUseCase
import org.noztek.esktransport.feature.passenger.location_search.presentation.LocationSearchViewModel
import org.noztek.esktransport.feature.passenger.ride_planner.data.impl.CurrentLocationRepositoryImpl
import org.noztek.esktransport.feature.passenger.ride_planner.data.impl.RidePlannerRepositoryImpl
import org.noztek.esktransport.feature.passenger.ride_planner.data.remote.RidePlannerApi
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.CurrentLocationRepository
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.RidePlannerRepository
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.GetNearbyDriversUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.GetRouteUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.ResolveCurrentLocationLabelUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.domain.usecase.ResolveCurrentLocationPointUseCase
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerViewModel
import org.noztek.esktransport.feature.passenger.settings.data.impl.SavedPlacesRepositoryImpl
import org.noztek.esktransport.feature.passenger.settings.data.remote.SavedPlacesApi
import org.noztek.esktransport.feature.passenger.settings.domain.repository.SavedPlacesRepository
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.CreateSavedPlaceUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.DeleteSavedPlaceUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.GetSavedPlacesUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.UpdateSavedPlaceUseCase
import org.noztek.esktransport.feature.passenger.settings.presentation.SavedPlacesViewModel
import org.noztek.esktransport.feature.passenger.trip_tracking.data.impl.TripTrackingRepositoryImpl
import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.TripTrackingApi
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.CancelPassengerTripUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.SubmitPassengerTripFeedbackUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.TripTrackingUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.presentation.TripTrackingViewModel
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionViewModel
import org.noztek.esktransport.feature.common.wallet.data.impl.WalletRepositoryImpl
import org.noztek.esktransport.feature.common.wallet.data.remote.WalletApi
import org.noztek.esktransport.feature.common.wallet.domain.repository.WalletRepository
import org.noztek.esktransport.feature.common.wallet.domain.usecase.GetWalletUseCase
import org.noztek.esktransport.feature.passenger.wallet.presentation.WalletViewModel

val passengerModule = module {
    single { KnownPlacesApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<KnownPlacesRepository> { KnownPlacesRepositoryImpl(api = get()) }
    single { GetKnownPlacesUseCase(repository = get()) }
    factory {
        PassengerHomeViewModel(
            getSavedPlacesUseCase = get(),
            getKnownPlacesUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single { KudiApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<KudiRepository> { KudiRepositoryImpl(api = get()) }
    single { GetCurrentKudiSessionUseCase(repository = get()) }
    single { CreateKudiSessionUseCase(repository = get()) }
    single { SendKudiMessageUseCase(repository = get()) }
    factory {
        KudiViewModel(
            getCurrentKudiSessionUseCase = get(),
            createKudiSessionUseCase = get(),
            sendKudiMessageUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single { MarketplaceApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<MarketplaceRepository> { MarketplaceRepositoryImpl(api = get()) }
    single { GetRentalVehicleTypesUseCase(repository = get()) }
    single { GetRentalListingsUseCase(repository = get()) }
    single { GetMarketplaceListingUseCase(repository = get()) }
    factory {
        MarketplaceViewModel(
            getRentalVehicleTypesUseCase = get(),
            getRentalListingsUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    factory {
        MarketplaceListingDetailsViewModel(
            getMarketplaceListingUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single<LocationRepository> { LocationRepositoryImpl(currentLocationProvider = get()) }
    single<PlaceSearchRepository> { PlaceSearchRepositoryImpl() }
    single<ReverseGeocodeRepository> { ReverseGeocodeRepositoryImpl(client = get(), mapboxConfig = get()) }
    single { GetCurrentLocationUseCase(locationRepository = get()) }
    single { SearchPlacesUseCase(placeSearchRepository = get()) }
    single { ResolveTapLabelUseCase(reverseGeocodeRepository = get()) }
    factory {
        LocationSearchViewModel(
            getCurrentLocationUseCase = get(),
            searchPlacesUseCase = get(),
            resolveTapLabelUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single<RidePlannerRepository> { RidePlannerRepositoryImpl(api = get(), mapboxDirectionsClient = get()) }
    single<CurrentLocationRepository> { CurrentLocationRepositoryImpl(currentLocationProvider = get()) }
    single { RidePlannerApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single { GetNearbyDriversUseCase(repository = get()) }
    single { GetRouteUseCase(repository = get()) }
    single { ResolveCurrentLocationLabelUseCase(repository = get()) }
    single { ResolveCurrentLocationPointUseCase(repository = get()) }
    factory {
        RidePlannerViewModel(
            resolveCurrentLocationLabelUseCase = get(),
            resolveCurrentLocationPointUseCase = get(),
            getNearbyDriversUseCase = get(),
            getRouteUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single { BookingReviewApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<BookingReviewRepository> { BookingReviewRepositoryImpl(api = get()) }
    single { CreateFareQuoteUseCase(repository = get()) }
    single { CreateBookingUseCase(repository = get()) }
    single { CancelBookingUseCase(repository = get()) }
    factory {
        BookingReviewViewModel(
            createFareQuoteUseCase = get(),
            createBookingUseCase = get(),
            cancelBookingUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
            realtimeCoordinator = get(),
        )
    }

    single { TripTrackingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<TripTrackingRepository> { TripTrackingRepositoryImpl(api = get()) }
    single { TripTrackingUseCase(repository = get()) }
    single { CancelPassengerTripUseCase(repository = get()) }
    single { SubmitPassengerTripFeedbackUseCase(repository = get()) }
    factory {
        TripTrackingViewModel(
            tripTrackingUseCase = get(),
            cancelPassengerTripUseCase = get(),
            submitPassengerTripFeedbackUseCase = get(),
            getPassengerActiveBookingUseCase = get(),
            mapboxDirectionsClient = get(),
            realtimeCoordinator = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    factory {
        PassengerSessionViewModel(
            getPassengerActiveBookingUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
    single { PassengerActivityApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<PassengerActivityRepository> { PassengerActivityRepositoryImpl(api = get()) }
    single { GetPassengerActivityUseCase(repository = get()) }
    factory {
        ActivityViewModel(
            getPassengerActiveBookingUseCase = get(),
            getPassengerActivityUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single { WalletApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<WalletRepository> { WalletRepositoryImpl(api = get()) }
    single { GetWalletUseCase(repository = get()) }
    factory {
        WalletViewModel(
            getWalletUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }

    single { SavedPlacesApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<SavedPlacesRepository> { SavedPlacesRepositoryImpl(api = get()) }
    single { GetSavedPlacesUseCase(repository = get()) }
    single { CreateSavedPlaceUseCase(repository = get()) }
    single { UpdateSavedPlaceUseCase(repository = get()) }
    single { DeleteSavedPlaceUseCase(repository = get()) }
    factory {
        SavedPlacesViewModel(
            getSavedPlacesUseCase = get(),
            createSavedPlaceUseCase = get(),
            updateSavedPlaceUseCase = get(),
            deleteSavedPlaceUseCase = get(),
            getCurrentLocationUseCase = get(),
            resolveTapLabelUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
