package com.example.kulnote.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.model.network.FileUploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.io.File
import java.io.FileOutputStream

object FileUploadHelper {
    suspend fun uploadImage(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileUploadHelper", "📤 Uploading image: $uri")
                
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    else -> "jpg"
                }
                val filename = "image_${System.currentTimeMillis()}.$extension"
                
                val tempFile = createTempFileFromUri(context, uri, "temp_image_")
                if (tempFile == null) {
                    return@withContext Result.failure<String>(Exception("Failed to create temp file"))
                }

                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", filename, requestFile)
                val typePart = "image".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.apiService.uploadFile(filePart, typePart).awaitResponse()

                tempFile.delete()

                if (response.isSuccessful) {
                    val url = response.body()?.data?.url
                    if (url != null) {
                        Log.d("FileUploadHelper", "✅ Upload success: $url")
                        Result.success<String>(url)
                    } else {
                        Log.e("FileUploadHelper", "❌ Upload failed: URL is null")
                        Result.failure<String>(Exception("Upload response URL is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FileUploadHelper", "❌ Upload failed: ${response.code()} - $errorBody")
                    Result.failure<String>(Exception("Upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("FileUploadHelper", "❌ Upload exception: ${e.message}", e)
                Result.failure<String>(e)
            }
        }
    }
    suspend fun uploadDocument(context: Context, uri: Uri, filename: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileUploadHelper", "📤 Uploading document: $filename")
                
                val tempFile = createTempFileFromUri(context, uri, "temp_doc_")
                if (tempFile == null) {
                    return@withContext Result.failure<String>(Exception("Failed to create temp file"))
                }

                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", filename, requestFile)
                val typePart = "document".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.apiService.uploadFile(filePart, typePart).awaitResponse()

                tempFile.delete()

                if (response.isSuccessful) {
                    val url = response.body()?.data?.url
                    if (url != null) {
                        Log.d("FileUploadHelper", "✅ Upload success: $url")
                        Result.success<String>(url)
                    } else {
                        Log.e("FileUploadHelper", "❌ Upload failed: URL is null")
                        Result.failure<String>(Exception("Upload response URL is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FileUploadHelper", "❌ Upload failed: ${response.code()} - $errorBody")
                    Result.failure<String>(Exception("Upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("FileUploadHelper", "❌ Upload exception: ${e.message}", e)
                Result.failure<String>(e)
            }
        }
    }
    private fun createTempFileFromUri(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile(prefix, null, context.cacheDir)
            
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            tempFile
        } catch (e: Exception) {
            Log.e("FileUploadHelper", "Failed to create temp file: ${e.message}", e)
            null
        }
    }
}
