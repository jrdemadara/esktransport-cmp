package org.noztek.esktransport.app.di.modules

import org.koin.dsl.module
import org.noztek.esktransport.app.navigation.StartupViewModel
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.core.session.data.AuthSessionRepositoryImpl
import org.noztek.esktransport.core.session.domain.AuthSessionRepository
import org.noztek.esktransport.core.session.domain.usecase.ClearSessionUseCase
import org.noztek.esktransport.core.session.domain.usecase.GetCachedSessionTokenUseCase
import org.noztek.esktransport.core.session.domain.usecase.HasSeenStarterUseCase
import org.noztek.esktransport.core.session.domain.usecase.LogoutUseCase
import org.noztek.esktransport.core.session.domain.usecase.MarkStarterSeenUseCase
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.core.session.domain.usecase.ObserveIsLoggedInUseCase
import org.noztek.esktransport.core.session.domain.usecase.ObserveSessionAccessTokenUseCase
import org.noztek.esktransport.core.session.domain.usecase.SaveSessionUseCase

val sessionModule = module {
    single { SessionManager(settings = get()) }

    single<AuthSessionRepository> { AuthSessionRepositoryImpl(sessionManager = get()) }
    single { ObserveCurrentSessionUseCase(repository = get()) }
    single { ObserveSessionAccessTokenUseCase(repository = get()) }
    single { ObserveIsLoggedInUseCase(repository = get()) }
    single { GetCachedSessionTokenUseCase(repository = get()) }
    single { SaveSessionUseCase(repository = get()) }
    single { ClearSessionUseCase(repository = get()) }
    single { LogoutUseCase(clearSessionUseCase = get()) }
    single { HasSeenStarterUseCase(repository = get()) }
    single { MarkStarterSeenUseCase(repository = get()) }

    single {
        StartupViewModel(
            observeCurrentSessionUseCase = get(),
            observeIsLoggedInUseCase = get(),
        )
    }
}
