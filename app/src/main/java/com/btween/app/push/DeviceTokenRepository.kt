package com.btween.app.push

import com.btween.app.data.remote.TokenManager
import com.btween.app.data.remote.api.DeviceApi
import com.btween.app.data.remote.dto.RegisterDeviceRequestDto
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenRepository @Inject constructor(
    private val deviceApi: DeviceApi,
    private val tokenManager: TokenManager
) {

    /** No-ops quietly if not logged in or the network call fails - this is best-effort
     * background plumbing, never something a user-facing action should block on or report
     * errors for. */
    suspend fun registerToken(fcmToken: String) {
        if (tokenManager.getAccessToken() == null) return
        try {
            deviceApi.registerDevice(RegisterDeviceRequestDto(fcmToken))
        } catch (e: Exception) {
            // Best-effort; a later app-open or token refresh will retry.
        }
    }

    /** Fetches the current FCM token and registers it - call after login/register and on
     * app start while already logged in, so a fresh install or a re-login on a new device
     * registers immediately rather than waiting for FCM's own token-rotation timing. */
    suspend fun registerCurrentToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            registerToken(token)
        } catch (e: Exception) {
            // Firebase not configured/reachable - fine, daily-quote push just won't work yet.
        }
    }

    suspend fun unregisterCurrentToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            deviceApi.unregisterDevice(RegisterDeviceRequestDto(token))
        } catch (e: Exception) {
            // Not critical if this fails on logout - the token will simply age out of
            // relevance once this device stops calling the API as this user.
        }
    }
}
