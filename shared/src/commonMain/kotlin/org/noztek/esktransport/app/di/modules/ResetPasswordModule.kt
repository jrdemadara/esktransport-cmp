package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.reset_password.data.impl.ResetPasswordRepositoryImpl
import org.noztek.esktransport.feature.common.reset_password.data.remote.ResetPasswordApi
import org.noztek.esktransport.feature.common.reset_password.domain.repository.ResetPasswordRepository
import org.noztek.esktransport.feature.common.reset_password.domain.usecase.ResetPasswordUseCase
import org.noztek.esktransport.feature.common.reset_password.presentation.ResetPasswordViewModel

val resetPasswordModule = module {
    single { ResetPasswordApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single<ResetPasswordRepository> { ResetPasswordRepositoryImpl(resetPasswordApi = get()) }
    single { ResetPasswordUseCase(repository = get()) }
    factory {
        ResetPasswordViewModel(
            resetPasswordUseCase = get(),
            otpStateStore = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
