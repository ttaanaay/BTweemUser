package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.SourceTypeResponseDto
import retrofit2.http.GET

interface SourceTypeApi {

    @GET("source-types")
    suspend fun getSourceTypes(): List<SourceTypeResponseDto>
}
