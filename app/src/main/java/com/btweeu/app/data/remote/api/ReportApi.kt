package com.btweeu.app.data.remote.api

import com.btweeu.app.data.remote.dto.ReportRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApi {

    @POST("reports")
    suspend fun submitReport(@Body request: ReportRequestDto)
}
