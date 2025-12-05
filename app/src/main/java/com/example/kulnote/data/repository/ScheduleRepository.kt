// FILE: ScheduleRepository.kt

package com.example.kulnote.data.repository

import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.network.ApiService
import com.example.kulnote.data.model.network.ScheduleRequest
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.model.network.ScheduleApiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class ScheduleRepository(
    private val apiService: ApiService,
    private val scheduleDao: ScheduleDao
) {

    // 1. READ: Aliran data utama dari Room (Offline-First)
    fun getSchedulesFlow(userId: String? = null): Flow<List<ScheduleEntity>> {
        return if (userId == null) scheduleDao.getAllSchedules()
        else scheduleDao.getSchedulesForUser(userId)
    }

    // 2. REFRESH: Ambil data dari Network dan simpan ke Room
    suspend fun refreshSchedules() {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ScheduleRepository", "📡 GET /api/schedules...")

                // Panggil API (membutuhkan token yang disiapkan di ApiClient)
                val response = apiService.getSchedules().awaitResponse()

                android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiData = response.body() ?: emptyList()
                    android.util.Log.d("ScheduleRepository", "📊 Data diterima: ${apiData.size} jadwal")

                    // Konversi ApiModel ke Entity Room
                    val entities = apiData.map { it.toEntity() }

                    // REPLACE ALL: Hapus seluruh jadwal lokal lalu simpan entri baru (atomik)
                    scheduleDao.replaceAll(entities)
                    android.util.Log.d("ScheduleRepository", "💾 Disimpan ke Room: ${entities.size} jadwal (replaced)")
                } else {
                    // Handle network error/unauthorized (misal: log error)
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ScheduleRepository", "❌ API Error ${response.code()}: $errorBody")
                    throw Exception("API Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                // Handle koneksi error
                android.util.Log.e("ScheduleRepository", "❌ Network Failure: ${e.message}", e)
                throw Exception("Network Failure: ${e.message}")
            }
        }
    }

    // 3. CREATE: Kirim Jadwal baru ke Network dan Refresh Lokal
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

            // Setelah berhasil disimpan di server, panggil refresh untuk update lokal
            android.util.Log.d("ScheduleRepository", "🔄 Refreshing local data...")
            refreshSchedules()
        }
    }
}

// Extension function untuk konversi data
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