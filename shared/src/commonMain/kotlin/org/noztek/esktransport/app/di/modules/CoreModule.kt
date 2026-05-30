package org.noztek.esktransport.app.di.modules

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.core.audio.createSoundEffectPlayer
import org.noztek.esktransport.core.realtime.BaseRealtimeCoordinator
import org.noztek.esktransport.core.realtime.DefaultBaseRealtimeCoordinator
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.createRealtimeClient
import org.noztek.esktransport.core.realtime.driver.PusherDriverBookingOfferRealtime
import org.noztek.esktransport.core.realtime.driver.DriverBookingOfferRealtime
import org.noztek.esktransport.core.realtime.passenger.PusherPassengerRealtimeCoordinator
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.core.session.SessionManager

const val IO_DISPATCHER_QUALIFIER = "io_dispatcher"

val coreModule = module {
    single<CoroutineDispatcher>(named(IO_DISPATCHER_QUALIFIER)) { Dispatchers.Default }
    single { createSoundEffectPlayer() }
    single {
        createRealtimeClient(
            config = get(),
            authTokenProvider = { get<SessionManager>().cachedToken() },
        )
    }
    single { RealtimeChannelNamer() }
    single<BaseRealtimeCoordinator> { DefaultBaseRealtimeCoordinator(realtimeClient = get()) }
    single<DriverBookingOfferRealtime> {
        PusherDriverBookingOfferRealtime(
            realtimeClient = get(),
            channelNamer = get(),
            sessionManager = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
            json = get(),
        )
    }
    single<PassengerRealtimeCoordinator> {
        PusherPassengerRealtimeCoordinator(
            realtimeClient = get(),
            channelNamer = get(),
            sessionManager = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
            json = get(),
        )
    }
}
