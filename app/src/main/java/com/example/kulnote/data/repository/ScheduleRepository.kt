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
    fun getSchedulesFlow(): Flow<List<ScheduleEntity>> {
        return scheduleDao.getAllSchedules()
    }

    // 2. REFRESH: Ambil data dari Network dan simpan ke Room
    suspend fun refreshSchedules() {
        withContext(Dispatchers.IO) {
            try {
                // Panggil API (membutuhkan token yang disiapkan di ApiClient)
                val response = apiService.getSchedules().awaitResponse()

                if (response.isSuccessful) {
                    val apiData = response.body() ?: emptyList()

                    // Konversi ApiModel ke Entity Room
                    val entities = apiData.map { it.toEntity() }

                    // Simpan ke Room (Ini akan memicu update ke UI via Flow)
                    scheduleDao.insertAll(entities)
                } else {
                    // Handle network error/unauthorized (misal: log error)
                    throw Exception("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                // Handle koneksi error
                throw Exception("Network Failure: ${e.message}")
            }
        }
    }

    // 3. CREATE: Kirim Jadwal baru ke Network dan Refresh Lokal
    suspend fun createSchedule(request: ScheduleRequest) {
        withContext(Dispatchers.IO) {
            val response = apiService.createSchedule(request).awaitResponse()
            if (!response.isSuccessful) {
                throw Exception("Gagal menyimpan Jadwal ke server: ${response.code()}")
            }
            // Setelah berhasil disimpan di server, panggil refresh untuk update lokal
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