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

    /**
     * Upload image to server and return URL
     */
    suspend fun uploadImage(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileUploadHelper", "📤 Uploading image: $uri")
                
                // 1. Get extension from MIME type
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    else -> "jpg"
                }
                val filename = "image_${System.currentTimeMillis()}.$extension"
                
                // 2. Copy URI content to temporary file
                val tempFile = createTempFileFromUri(context, uri, "temp_image_")
                if (tempFile == null) {
                    return@withContext Result.failure<String>(Exception("Failed to create temp file"))
                }

                // 3. Prepare multipart request with correct filename
                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", filename, requestFile)
                val typePart = "image".toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Upload to server
                val response = ApiClient.apiService.uploadFile(filePart, typePart).awaitResponse()

                // 4. Cleanup temp file
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

    /**
     * Upload document to server and return URL
     */
    suspend fun uploadDocument(context: Context, uri: Uri, filename: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileUploadHelper", "📤 Uploading document: $filename")
                
                // 1. Copy URI content to temporary file
                val tempFile = createTempFileFromUri(context, uri, "temp_doc_")
                if (tempFile == null) {
                    return@withContext Result.failure<String>(Exception("Failed to create temp file"))
                }

                // 2. Prepare multipart request
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", filename, requestFile)
                val typePart = "document".toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Upload to server
                val response = ApiClient.apiService.uploadFile(filePart, typePart).awaitResponse()

                // 4. Cleanup temp file
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

    /**
     * Create temporary file from URI
     */
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
