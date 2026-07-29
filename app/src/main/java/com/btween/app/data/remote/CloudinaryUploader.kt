package com.btween.app.data.remote

import android.content.Context
import android.net.Uri
import com.btween.app.di.CloudinaryCloudName
import com.btween.app.di.CloudinaryUploadPreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    @CloudinaryCloudName private val cloudName: String,
    @CloudinaryUploadPreset private val uploadPreset: String
) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /** Returns the resulting HTTPS URL of the uploaded image, or a failure. */
    suspend fun uploadImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tempFile = copyUriToTempFile(uri)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart(
                    "file",
                    tempFile.name,
                    tempFile.asRequestBody("image/*".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                tempFile.delete()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Upload failed (${response.code}) - check your Cloudinary cloud name/preset")
                    )
                }
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                val secureUrl = json.parseToJsonElement(body).jsonObject["secure_url"]?.jsonPrimitive?.content
                    ?: return@withContext Result.failure(Exception("Upload succeeded but no URL was returned"))
                Result.success(secureUrl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyUriToTempFile(uri: Uri): File {
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(tempFile).use { output ->
                input?.copyTo(output)
            }
        }
        return tempFile
    }
}
