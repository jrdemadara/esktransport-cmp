package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.forgot_password.data.impl.ForgotPasswordRepositoryImpl
import org.noztek.esktransport.feature.common.forgot_password.data.remote.ForgotPasswordApi
import org.noztek.esktransport.feature.common.forgot_password.domain.repository.ForgotPasswordRepository
import org.noztek.esktransport.feature.common.forgot_password.domain.usecase.ForgotPasswordUseCase
import org.noztek.esktransport.feature.common.forgot_password.presentation.ForgotPasswordViewModel

val forgotPasswordModule = module {
    single { ForgotPasswordApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<ForgotPasswordRepository> { ForgotPasswordRepositoryImpl(forgotPasswordApi = get()) }
    single { ForgotPasswordUseCase(repository = get()) }
    factory {
        ForgotPasswordViewModel(
            forgotPasswordUseCase = get(),
            otpStateStore = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
