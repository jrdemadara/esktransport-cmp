package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.register.data.impl.RegisterRepositoryImpl
import org.noztek.esktransport.feature.common.register.data.remote.RegisterApi
import org.noztek.esktransport.feature.common.register.domain.repository.RegisterRepository
import org.noztek.esktransport.feature.common.register.domain.usecase.RegisterUserUseCase
import org.noztek.esktransport.feature.common.register.presentation.RegisterViewModel

val registerModule = module {
    single { RegisterApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<RegisterRepository> { RegisterRepositoryImpl(registerApi = get()) }
    single { RegisterUserUseCase(repository = get()) }
    factory {
        RegisterViewModel(
            registerUserUseCase = get(),
            otpStateStore = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
