package com.btweeu.app.data.repository

import com.btweeu.app.data.remote.api.MaintenanceApi
import com.btweeu.app.data.remote.safeApiCall
import com.btweeu.app.domain.repository.MaintenanceRepository
import com.btweeu.app.domain.repository.MaintenanceStatus
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
