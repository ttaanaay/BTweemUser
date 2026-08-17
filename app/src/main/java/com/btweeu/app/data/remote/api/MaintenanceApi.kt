package com.btweeu.app.data.remote.api

import com.btweeu.app.data.remote.dto.MaintenanceStatusResponseDto
import retrofit2.http.GET

interface MaintenanceApi {

    @GET("maintenance-status")
    suspend fun getMaintenanceStatus(): MaintenanceStatusResponseDto
}
