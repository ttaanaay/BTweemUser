package com.btween.app.domain.repository

data class MaintenanceStatus(val enabled: Boolean, val message: String?)

interface MaintenanceRepository {
    suspend fun getStatus(): Result<MaintenanceStatus>
}
