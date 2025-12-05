// FILE: ScheduleViewModel.kt (Modifikasi)

package com.example.kulnote.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kulnote.data.local.db.AppDatabase
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.model.MataKuliah
import com.example.kulnote.data.model.ScheduleInput
import com.example.kulnote.data.model.network.ScheduleRequest
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.network.SessionManager
import com.example.kulnote.data.repository.ScheduleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
// Ganti ViewModel menjadi AndroidViewModel karena membutuhkan Context
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    // --- INJEKSI DEPENDENCY ---
    private val database = AppDatabase.getDatabase(application)
    private val repository = ScheduleRepository(
        apiService = ApiClient.apiService,
        scheduleDao = database.scheduleDao()
    )

    // --- STATE FLOW BARU (Mengambil dari Repository/Room) ---
    // Mengubah ScheduleEntity lokal menjadi MataKuliah Model yang digunakan UI
    val mataKuliahList: StateFlow<List<MataKuliah>> =
        SessionManager.currentUserId
            .flatMapLatest { userId ->
                repository.getSchedulesFlow(userId)
            }
            .map { entities -> entities.map { it.toUiModel() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Inisialisasi: Panggil refresh jika token tersedia
    init {
        // Jika token tersedia saat startup, lakukan satu kali refresh
        if (ApiClient.authToken != null) {
            refreshDataFromNetwork()
        } else {
            android.util.Log.d("ScheduleViewModel", "⚠️ Skip refresh: authToken belum tersedia")
        }

        // Observasi perubahan currentUserId dan refresh saat user baru login
        viewModelScope.launch {
            var lastUserId: String? = null
            SessionManager.currentUserId.collect { userId ->
                if (userId != null && userId != lastUserId) {
                    lastUserId = userId
                    android.util.Log.d("ScheduleViewModel", "🔁 Detected currentUserId change: $userId — refreshing")
                    refreshDataFromNetwork()
                }
            }
        }
    }

    // --- FUNGSI BARU UNTUK KOMUNIKASI NETWORK ---

    // Fungsi untuk menarik data terbaru dari server
    fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ScheduleViewModel", "🔄 Refreshing schedules from network...")
                repository.refreshSchedules()
                android.util.Log.d("ScheduleViewModel", "✅ Refresh berhasil!")
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "❌ Refresh gagal: ${e.message}", e)
                // Nanti: Tampilkan Toast/Snackbar error di UI
            }
        }
    }

    // --- FUNGSI SAVE BARU (Menggunakan Repository) ---

    fun saveNewSchedule(input: ScheduleInput) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ScheduleViewModel", "💾 Menyimpan jadwal baru: ${input.namaMatkul}")

                // 1. Konversi ScheduleInput ke ScheduleRequest untuk Network
                val request = ScheduleRequest(
                    namaMatakuliah = input.namaMatkul,
                    sks = input.sks.toIntOrNull() ?: 0,
                    dosen = input.dosen,
                    hari = input.hari,
                    jamMulai = input.jamMulai.take(4).chunked(2).joinToString(":") + ":00",
                    jamSelesai = input.jamSelesai.take(4).chunked(2).joinToString(":") + ":00",
                    ruangan = input.ruangan
                )

                android.util.Log.d("ScheduleViewModel", "📤 Request: $request")

                // 2. Kirim ke Repository (yang akan mengirim ke Server & Refresh Room)
                repository.createSchedule(request)

                android.util.Log.d("ScheduleViewModel", "✅ Jadwal berhasil disimpan!")

            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "❌ Gagal menyimpan jadwal: ${e.message}", e)
            }
        }
    }
}

// --- EXTENSION UNTUK MAPPING ---
// Anda perlu membuat model MataKuliah dan ScheduleRequest agar mapping ini berfungsi

// Mapping dari ScheduleEntity (Room) ke MataKuliah (UI Model)
fun ScheduleEntity.toUiModel(): MataKuliah {
    return MataKuliah(
        id = this.id,
        namaMatkul = this.namaMatakuliah,
        sks = this.sks,
        dosen = this.dosen,
        hari = this.hari,
        jamMulai = this.jamMulai,
        jamSelesai = this.jamSelesai,
        ruangan = this.ruangan
    )
}