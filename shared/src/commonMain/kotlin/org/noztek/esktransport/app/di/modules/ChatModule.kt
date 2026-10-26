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
import org.noztek.esktransport.feature.common.marketplace_chat.data.impl.MarketplaceChatRepositoryImpl
import org.noztek.esktransport.feature.common.marketplace_chat.data.realtime.MarketplaceChatRealtime
import org.noztek.esktransport.feature.common.marketplace_chat.data.realtime.PusherMarketplaceChatRealtime
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.MarketplaceChatApi
import org.noztek.esktransport.feature.common.marketplace_chat.domain.repository.MarketplaceChatRepository
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.GetOwnerMarketplaceMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.MarkOwnerMarketplaceThreadReadUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.GetPassengerMarketplaceMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.ObserveMarketplaceChatMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.OpenPassengerMarketplaceConversationUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SendOwnerMarketplaceMessageUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SendPassengerMarketplaceMessageUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.UnsubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.presentation.MarketplaceChatViewModel

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

    single { MarketplaceChatApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<MarketplaceChatRepository> { MarketplaceChatRepositoryImpl(api = get()) }
    single<MarketplaceChatRealtime> {
        PusherMarketplaceChatRealtime(
            realtimeClient = get(),
            channelNamer = get(),
            sessionManager = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
            json = get(),
        )
    }
    single { OpenPassengerMarketplaceConversationUseCase(repository = get()) }
    single { GetPassengerMarketplaceMessagesUseCase(repository = get()) }
    single { SendPassengerMarketplaceMessageUseCase(repository = get()) }
    single { GetOwnerMarketplaceMessagesUseCase(repository = get()) }
    single { MarkOwnerMarketplaceThreadReadUseCase(repository = get()) }
    single { SendOwnerMarketplaceMessageUseCase(repository = get()) }
    single { SubscribeMarketplaceChatUseCase(realtime = get()) }
    single { UnsubscribeMarketplaceChatUseCase(realtime = get()) }
    single { ObserveMarketplaceChatMessagesUseCase(realtime = get()) }
    factory {
        MarketplaceChatViewModel(
            openPassengerMarketplaceConversationUseCase = get(),
            getOwnerMarketplaceMessagesUseCase = get(),
            markOwnerMarketplaceThreadReadUseCase = get(),
            sendPassengerMarketplaceMessageUseCase = get(),
            sendOwnerMarketplaceMessageUseCase = get(),
            subscribeMarketplaceChatUseCase = get(),
            unsubscribeMarketplaceChatUseCase = get(),
            observeMarketplaceChatMessagesUseCase = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
