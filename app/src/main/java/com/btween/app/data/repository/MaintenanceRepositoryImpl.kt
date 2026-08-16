package com.btween.app.data.repository

import com.btween.app.data.remote.api.MaintenanceApi
import com.btween.app.data.remote.safeApiCall
import com.btween.app.domain.repository.MaintenanceRepository
import com.btween.app.domain.repository.MaintenanceStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceApi: MaintenanceApi
) : MaintenanceRepository {

    override suspend fun getStatus(): Result<MaintenanceStatus> = safeApiCall {
        val response = maintenanceApi.getMaintenanceStatus()
        MaintenanceStatus(response.enabled, response.message)
    }
}
