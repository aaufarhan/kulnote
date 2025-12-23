package com.example.kulnote.data.repository

import android.content.Context
import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.model.network.ScheduleApiModel
import com.example.kulnote.data.model.network.ScheduleRequest
import com.example.kulnote.data.network.ApiService
import com.example.kulnote.data.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class ScheduleRepository(
    private val apiService: ApiService,
    private val scheduleDao: ScheduleDao,
    context: Context
) {

    private val scheduler = ScheduleAlarmScheduler(context)

    fun getSchedulesFlow(userId: String? = null): Flow<List<ScheduleEntity>> {
        return if (userId == null) scheduleDao.getAllSchedules()
        else scheduleDao.getSchedulesForUser(userId)
    }

    suspend fun refreshSchedules() {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ScheduleRepository", "📡 GET /api/schedules...")

                val response = apiService.getSchedules().awaitResponse()

                android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiData = response.body() ?: emptyList()
                    android.util.Log.d("ScheduleRepository", "📊 Data diterima: ${apiData.size} jadwal")

                    val entities = apiData.map { it.toEntity() }

                    scheduleDao.replaceAll(entities)
                    android.util.Log.d("ScheduleRepository", "💾 Disimpan ke Room: ${entities.size} jadwal (replaced)")

                    val currentUserId = SessionManager.currentUserId.value
                    entities.filter { it.userId == currentUserId }.forEach { scheduler.schedule(it) }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleRepository", "❌ API Error ${response.code()}: $errorBody")
                    throw Exception("API Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleRepository", "❌ Network Failure: ${e.message}", e)
                throw Exception("Network Failure: ${e.message}")
            }
        }
    }

    suspend fun createSchedule(request: ScheduleRequest) {
        withContext(Dispatchers.IO) {
            android.util.Log.d("ScheduleRepository", "📤 POST /api/schedules")
            android.util.Log.d("ScheduleRepository", "📦 Request Body: $request")

            val response = apiService.createSchedule(request).awaitResponse()

            android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ScheduleRepository", "❌ Save Error ${response.code()}: $errorBody")
                throw Exception("Gagal menyimpan Jadwal ke server: ${response.code()} - $errorBody")
            }

            val savedSchedule = response.body()
            android.util.Log.d("ScheduleRepository", "✅ Jadwal tersimpan di server: $savedSchedule")

            android.util.Log.d("ScheduleRepository", "🔄 Refreshing local data...")
            refreshSchedules()
        }
    }

    suspend fun updateSchedule(scheduleId: String, request: ScheduleRequest) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ScheduleRepository", "📤 PUT /api/schedules/$scheduleId")

                val response = apiService.updateSchedule(scheduleId, request).awaitResponse()

                android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleRepository", "❌ Update Error ${response.code()}: $errorBody")
                    throw Exception("Gagal update jadwal: ${response.code()} - $errorBody")
                }

                refreshSchedules()
                android.util.Log.d("ScheduleRepository", "✅ Jadwal berhasil diupdate dan lokal disinkronkan")
            } catch (e: Exception) {
                android.util.Log.e("ScheduleRepository", "❌ Update Error: ${e.message}", e)
                throw e
            }
        }
    }

    suspend fun deleteSchedule(scheduleId: String) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ScheduleRepository", "📤 DELETE /api/schedules/$scheduleId")

                val response = apiService.deleteSchedule(scheduleId).awaitResponse()

                android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleRepository", "❌ Delete Error ${response.code()}: $errorBody")
                    throw Exception("Gagal hapus jadwal: ${response.code()} - $errorBody")
                }

                scheduleDao.deleteById(scheduleId)
                scheduler.cancel(scheduleId)
                android.util.Log.d("ScheduleRepository", "✅ Jadwal dihapus dari lokal")
            } catch (e: Exception) {
                android.util.Log.e("ScheduleRepository", "❌ Delete Error: ${e.message}", e)
                throw e
            }
        }
    }
}
fun ScheduleApiModel.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = this.id,
        userId = this.userId,
        namaMatakuliah = this.namaMatakuliah,
        sks = this.sks,
        dosen = this.dosen,
        hari = this.hari,
        jamMulai = this.jamMulai,
        jamSelesai = this.jamSelesai,
        ruangan = this.ruangan
    )
}