package org.noztek.esktransport.core.session.domain.usecase

class LogoutUseCase(
    private val clearSessionUseCase: ClearSessionUseCase,
) {
    operator fun invoke() {
        clearSessionUseCase()
    }
}
