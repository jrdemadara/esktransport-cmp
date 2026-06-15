package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.active_booking.data.impl.ActiveBookingRepositoryImpl
import org.noztek.esktransport.feature.common.active_booking.data.remote.ActiveBookingApi
import org.noztek.esktransport.feature.common.active_booking.domain.repository.ActiveBookingRepository
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetDriverActiveBookingUseCase
import org.noztek.esktransport.feature.common.active_booking.domain.usecase.GetPassengerActiveBookingUseCase

val activeBookingModule = module {
    single { ActiveBookingApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<ActiveBookingRepository> { ActiveBookingRepositoryImpl(api = get()) }
    single { GetPassengerActiveBookingUseCase(repository = get()) }
    single { GetDriverActiveBookingUseCase(repository = get()) }
}
