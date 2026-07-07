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
import org.noztek.esktransport.feature.passenger.trip_tracking.data.impl.TripTrackingRepositoryImpl
import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.TripTrackingApi
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.CancelPassengerTripUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.SubmitPassengerTripFeedbackUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.usecase.TripTrackingUseCase
import org.noztek.esktransport.feature.passenger.trip_tracking.presentation.TripTrackingViewModel
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionViewModel

val passengerModule = module {
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
}
