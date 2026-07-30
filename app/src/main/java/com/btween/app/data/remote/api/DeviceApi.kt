package com.btween.app.data.remote.api

import com.btween.app.data.remote.dto.RegisterDeviceRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApi {

    @POST("devices/register")
    suspend fun registerDevice(@Body request: RegisterDeviceRequestDto)

    @POST("devices/unregister")
    suspend fun unregisterDevice(@Body request: RegisterDeviceRequestDto)
}
