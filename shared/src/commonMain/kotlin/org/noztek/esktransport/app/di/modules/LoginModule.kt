package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.login.data.impl.LoginRepositoryImpl
import org.noztek.esktransport.feature.common.login.data.remote.LoginApi
import org.noztek.esktransport.feature.common.login.domain.repository.LoginRepository
import org.noztek.esktransport.feature.common.login.domain.usecase.LoginUseCase
import org.noztek.esktransport.feature.common.login.presentation.LoginViewModel

val loginModule = module {
    single { LoginApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<LoginRepository> { LoginRepositoryImpl(loginApi = get(), sessionManager = get()) }
    single { LoginUseCase(repository = get()) }
    factory { LoginViewModel(loginUseCase = get(), ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER))) }
}
