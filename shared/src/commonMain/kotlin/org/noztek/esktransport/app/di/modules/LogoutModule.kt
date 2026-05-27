package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.logout.data.impl.LogoutRepositoryImpl
import org.noztek.esktransport.feature.common.logout.data.remote.LogoutApi
import org.noztek.esktransport.feature.common.logout.domain.repository.LogoutRepository
import org.noztek.esktransport.feature.common.logout.domain.usecase.LogoutUseCase
import org.noztek.esktransport.feature.common.logout.presentation.LogoutViewModel

val logoutModule = module {
    single { LogoutApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<LogoutRepository> { LogoutRepositoryImpl(logoutApi = get(), sessionManager = get()) }
    single { LogoutUseCase(repository = get()) }
    factory { LogoutViewModel(logoutUseCase = get(), ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER))) }
}
