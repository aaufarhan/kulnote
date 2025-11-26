// FILE: ScheduleRepository.kt (BACKUP - Untuk handle wrapped response)
// GUNAKAN FILE INI JIKA Laravel server mengirim wrapped object seperti:
// { "status": "success", "data": [...] }

package com.example.kulnote.data.repository

import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.network.ApiService
import com.example.kulnote.data.model.network.ScheduleRequest
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.model.network.ScheduleApiModel
import com.example.kulnote.data.model.network.ScheduleListResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class ScheduleRepository(
    private val apiService: ApiService,
    private val scheduleDao: ScheduleDao
) {

    // 1. READ: Aliran data utama dari Room (Offline-First)
    fun getSchedulesFlow(): Flow<List<ScheduleEntity>> {
        return scheduleDao.getAllSchedules()
    }

    // 2. REFRESH: Ambil data dari Network dan simpan ke Room
    // VERSI UNTUK WRAPPED RESPONSE
    suspend fun refreshSchedules() {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ScheduleRepository", "📡 GET /api/schedules...")

                // UBAH: Panggil getSchedulesWrapped() untuk wrapped response
                val response = apiService.getSchedulesWrapped().awaitResponse()

                android.util.Log.d("ScheduleRepository", "📥 Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    // UBAH: Ekstrak data dari wrapper
                    val apiData = responseBody?.data ?: emptyList()

                    android.util.Log.d("ScheduleRepository", "📊 Data diterima: ${apiData.size} jadwal")

                    // Konversi ApiModel ke Entity Room
                    val entities = apiData.map { it.toEntity() }

                    // Simpan ke Room (Ini akan memicu update ke UI via Flow)
                    scheduleDao.insertAll(entities)
                    android.util.Log.d("ScheduleRepository", "💾 Disimpan ke Room: ${entities.size} jadwal")
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

/*
CARA PAKAI FILE INI:

1. Backup ScheduleRepository.kt yang lama
2. Replace dengan file ini
3. Update ApiService.kt:
   - Comment: fun getSchedules(): Call<List<ScheduleApiModel>>
   - Uncomment: fun getSchedulesWrapped(): Call<ScheduleListResponse>

   ATAU tambahkan method baru:

   @GET("schedules")
   fun getSchedulesWrapped(): Call<ScheduleListResponse>

4. Rebuild project
5. Run app

CATATAN:
- Gunakan ini HANYA jika Laravel tidak bisa diubah
- Solusi terbaik tetap fix Laravel Controller
*/

