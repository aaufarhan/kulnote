package com.example.kulnote.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.kulnote.data.local.dao.ReminderDao
import com.example.kulnote.data.local.dao.ReminderFileDao
import com.example.kulnote.data.local.model.ReminderEntity
import com.example.kulnote.data.local.model.ReminderFileEntity
import com.example.kulnote.data.model.ReminderInput
import com.example.kulnote.data.model.network.ReminderNetworkModel
import com.example.kulnote.data.model.network.ReminderRequest
import com.example.kulnote.data.network.ApiService
import com.example.kulnote.data.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ReminderRepository(
    private val apiService: ApiService,
    private val reminderDao: ReminderDao,
    private val reminderFileDao: ReminderFileDao,
    private val context: Context
) {
    private val BASE_URL = "http://192.168.1.27:8000"
    private val alarmScheduler = ReminderAlarmScheduler(context)

    val allReminders: Flow<List<ReminderEntity>> = SessionManager.currentUserId.flatMapLatest { userId ->
        reminderDao.getRemindersByUser(userId ?: "")
    }
    fun getFilesForReminder(reminderId: String): Flow<List<ReminderFileEntity>> =
        reminderFileDao.getFilesForReminder(reminderId)

    suspend fun refreshReminders() {
        try {
            val currentUserId = SessionManager.currentUserId.value ?: return
            val response = apiService.getReminders()
            if (response.isSuccessful) {
                val reminders = response.body()?.data ?: emptyList()
                val entities = reminders.map { it.toEntity() }

                // Opsional: Hapus data lama milik user ini sebelum insert yang baru (clean sync)
                // reminderDao.deleteByUserId(currentUserId)

                reminderDao.insertAll(entities)

                entities.forEach { entity ->
                    // Pastikan hanya menjadwalkan alarm jika userId-nya cocok
                    if (entity.userId == currentUserId) {
                        alarmScheduler.schedule(entity)
                    }
                }
                reminders.forEach { reminder ->
                    syncFilesForReminder(reminder.id)
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error refresh: ${e.message}")
        }
    }

    private suspend fun syncFilesForReminder(reminderId: String) {
        try {
            val response = apiService.getReminderFiles(reminderId)
            if (response.isSuccessful) {
                val files = response.body()?.data ?: emptyList()
                
                reminderFileDao.deleteFilesForReminder(reminderId)

                val fileEntities = files.map { networkFile ->
                    ReminderFileEntity(
                        idFile = networkFile.idFile,
                        idReminder = networkFile.idReminder ?: reminderId,
                        namaFile = networkFile.namaFile,
                        tipeFile = networkFile.tipeFile,
                        remoteUrl = formatFullUrl(networkFile.url),
                        localPath = null
                    )
                }
                if (fileEntities.isNotEmpty()) {
                    reminderFileDao.insertAll(fileEntities)
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error sync files: ${e.message}")
        }
    }

    private fun formatFullUrl(url: String?): String? {
        if (url == null) return null
        if (url.startsWith("http")) return url
        return "$BASE_URL${if (url.startsWith("/")) "" else "/"}$url"
    }

    suspend fun createReminder(input: ReminderInput, fileUri: Uri? = null) {
        try {
            val request = ReminderRequest(
                jenisReminder = input.subject,
                tanggal = input.date,
                jam = "${input.time}:00",
                keterangan = input.description
            )
            
            val response = apiService.createReminder(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val newReminder = response.body()!!.data!!
                val entity = newReminder.toEntity()
                reminderDao.insert(entity)

                // Jadwalkan alarm untuk reminder baru
                alarmScheduler.schedule(entity)

                if (fileUri != null) {
                    uploadAndSaveFile(newReminder.id, fileUri)
                }
                
                refreshReminders()
            }
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error create: ${e.message}")
        }
    }

    private suspend fun uploadAndSaveFile(reminderId: String, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val tempFile = copyUriToTempFile(uri) ?: return@withContext
            
            val mimeType = context.contentResolver.getType(uri)
            val requestFile = tempFile.asRequestBody(mimeType?.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val idReminderPart = reminderId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.uploadFileToReminder(filePart, idReminderPart)
            if (response.isSuccessful && response.body()?.data != null) {
                val networkFile = response.body()!!.data!!
                
                val internalFile = saveToInternalStorage(tempFile, networkFile.namaFile)
                
                val fileEntity = ReminderFileEntity(
                    idFile = networkFile.idFile,
                    idReminder = reminderId,
                    namaFile = networkFile.namaFile,
                    tipeFile = networkFile.tipeFile,
                    remoteUrl = formatFullUrl(networkFile.url),
                    localPath = internalFile.absolutePath
                )
                reminderFileDao.insertFile(fileEntity)
            }
            tempFile.delete()
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error upload: ${e.message}")
        }
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            val extension = mime.getExtensionFromMimeType(contentResolver.getType(uri))
            
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.${extension ?: "tmp"}")
            
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error copying file: ${e.message}")
            null
        }
    }

    private fun saveToInternalStorage(file: File, fileName: String): File {
        val dir = File(context.filesDir, "reminders")
        if (!dir.exists()) dir.mkdirs()
        
        val cleanName = fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val destFile = File(dir, "${System.currentTimeMillis()}_$cleanName")
        file.copyTo(destFile, overwrite = true)
        return destFile
    }

    private fun ReminderNetworkModel.toEntity() = ReminderEntity(
        id = id,
        userId = userId.toString(),
        judul = jenisReminder,
        deskripsi = keterangan,
        waktuReminder = "$tanggal $jam",
        isCompleted = isCompleted == 1,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    suspend fun updateReminder(reminderId: String, input: ReminderInput) {
        try {
            val request = ReminderRequest(
                jenisReminder = input.subject,
                tanggal = input.date,
                jam = if (input.time.length == 5) "${input.time}:00" else input.time,
                keterangan = input.description
            )
            val response = apiService.updateReminder(reminderId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                val updatedReminder = response.body()!!.data!!
                val entity = updatedReminder.toEntity()
                reminderDao.insert(entity) // Room akan me-replace karena ID sama
                alarmScheduler.schedule(entity) // Update alarm ke waktu baru
            }
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error update: ${e.message}")
        }
    }

    suspend fun deleteReminder(reminderId: String) {
        try {
            val response = apiService.deleteReminder(reminderId)
            if (response.isSuccessful) {
                reminderDao.deleteById(reminderId)
                // Opsional: Batalkan alarm jika diperlukan
            }
        } catch (e: Exception) {
            Log.e("ReminderRepo", "Error delete: ${e.message}")
        }
    }
}
