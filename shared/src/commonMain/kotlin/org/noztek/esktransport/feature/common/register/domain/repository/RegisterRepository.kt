package org.noztek.esktransport.feature.common.register.domain.repository

import org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload

interface RegisterRepository {
    suspend fun register(payload: org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload): Result<Unit>
}
