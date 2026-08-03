package org.noztek.esktransport.app.di.modules

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.core.audio.createSoundEffectPlayer
import org.noztek.esktransport.core.notify.PushNotificationTokenProvider
import org.noztek.esktransport.core.notify.createPushNotificationTokenProvider
import org.noztek.esktransport.core.realtime.BaseRealtimeCoordinator
import org.noztek.esktransport.core.realtime.DefaultBaseRealtimeCoordinator
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.createRealtimeClient
import org.noztek.esktransport.core.realtime.driver.PusherDriverBookingOfferRealtime
import org.noztek.esktransport.core.realtime.driver.DriverBookingOfferRealtime
import org.noztek.esktransport.core.realtime.driver.DriverOnboardingRealtime
import org.noztek.esktransport.core.realtime.driver.PusherDriverOnboardingRealtime
import org.noztek.esktransport.core.realtime.passenger.PusherPassengerRealtimeCoordinator
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.presence.data.impl.UserPresenceRepositoryImpl
import org.noztek.esktransport.feature.common.presence.data.remote.UserPresenceApi
import org.noztek.esktransport.feature.common.presence.domain.lifecycle.UserPresenceCoordinator
import org.noztek.esktransport.feature.common.presence.domain.repository.UserPresenceRepository
import org.noztek.esktransport.feature.common.presence.domain.usecase.GetUserPresenceUseCase
import org.noztek.esktransport.feature.common.presence.domain.usecase.MarkUserBackgroundUseCase
import org.noztek.esktransport.feature.common.presence.domain.usecase.MarkUserForegroundUseCase
import org.noztek.esktransport.feature.common.presence.domain.usecase.MarkUserOfflineUseCase
import org.noztek.esktransport.feature.common.presence.domain.usecase.SendUserHeartbeatUseCase

const val IO_DISPATCHER_QUALIFIER = "io_dispatcher"

val coreModule = module {
    single<CoroutineDispatcher>(named(IO_DISPATCHER_QUALIFIER)) { Dispatchers.Default }
    single { createSoundEffectPlayer() }
    single<PushNotificationTokenProvider> { createPushNotificationTokenProvider() }
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
    single<DriverOnboardingRealtime> {
        PusherDriverOnboardingRealtime(
            realtimeClient = get(),
            channelNamer = get(),
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
    single { UserPresenceApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<UserPresenceRepository> { UserPresenceRepositoryImpl(api = get()) }
    single { GetUserPresenceUseCase(repository = get()) }
    single { SendUserHeartbeatUseCase(repository = get()) }
    single { MarkUserForegroundUseCase(repository = get()) }
    single { MarkUserBackgroundUseCase(repository = get()) }
    single { MarkUserOfflineUseCase(repository = get()) }
    single {
        UserPresenceCoordinator(
            sessionManager = get(),
            markUserForegroundUseCase = get(),
            markUserOfflineUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
