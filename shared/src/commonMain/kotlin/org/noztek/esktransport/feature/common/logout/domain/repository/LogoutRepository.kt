package org.noztek.esktransport.feature.common.logout.domain.repository

interface LogoutRepository {
    suspend fun logout(): Result<Unit>
}