package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.chat.data.impl.TripChatRepositoryImpl
import org.noztek.esktransport.feature.common.chat.data.realtime.PusherTripChatRealtime
import org.noztek.esktransport.feature.common.chat.data.realtime.TripChatRealtime
import org.noztek.esktransport.feature.common.chat.data.remote.TripChatApi
import org.noztek.esktransport.feature.common.chat.domain.repository.TripChatRepository
import org.noztek.esktransport.feature.common.chat.domain.usecase.GetTripChatMessagesUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.ObserveTripChatMessagesUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.SendTripChatMessageUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.SubscribeTripChatUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.UnsubscribeTripChatUseCase
import org.noztek.esktransport.feature.common.chat.presentation.TripChatViewModel

val chatModule = module {
    single { TripChatApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<TripChatRepository> { TripChatRepositoryImpl(api = get()) }
    single<TripChatRealtime> {
        PusherTripChatRealtime(
            realtimeClient = get(),
            channelNamer = get(),
            sessionManager = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
            json = get(),
        )
    }
    single { GetTripChatMessagesUseCase(repository = get()) }
    single { SendTripChatMessageUseCase(repository = get()) }
    single { SubscribeTripChatUseCase(realtime = get()) }
    single { UnsubscribeTripChatUseCase(realtime = get()) }
    single { ObserveTripChatMessagesUseCase(realtime = get()) }
    factory {
        TripChatViewModel(
            getTripChatMessagesUseCase = get(),
            sendTripChatMessageUseCase = get(),
            subscribeTripChatUseCase = get(),
            unsubscribeTripChatUseCase = get(),
            observeTripChatMessagesUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
