package org.noztek.esktransport.app.di.modules

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.feature.common.otp.data.impl.OtpRepositoryImpl
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import org.noztek.esktransport.feature.common.otp.data.remote.OtpApi
import org.noztek.esktransport.feature.common.otp.domain.repository.OtpRepository
import org.noztek.esktransport.feature.common.otp.domain.usecase.RequestOtpUseCase
import org.noztek.esktransport.feature.common.otp.domain.usecase.VerifyOtpUseCase
import org.noztek.esktransport.feature.common.otp.presentation.OtpViewModel

val otpModule = module {
    single { OtpApi(client = get(), baseUrl = get(named(API_BASE_URL_QUALIFIER))) }
    single { OtpStateStore(settings = get()) }
    single<OtpRepository> { OtpRepositoryImpl(otpApi = get()) }
    single { RequestOtpUseCase(repository = get()) }
    single { VerifyOtpUseCase(repository = get()) }
    factory {
        OtpViewModel(
            requestOtpUseCase = get(),
            verifyOtpUseCase = get(),
            stateStore = get(),
            ioDispatcher = get(named(IO_DISPATCHER_QUALIFIER)),
        )
    }
}
