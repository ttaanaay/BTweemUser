package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.MaintenanceStatusResponseDto
import retrofit2.http.GET

interface MaintenanceApi {

    @GET("maintenance-status")
    suspend fun getMaintenanceStatus(): MaintenanceStatusResponseDto
}
