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
import com.example.kulnote.data.repository.ScheduleRepository
import com.example.kulnote.data.network.ApiClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val mataKuliahList: StateFlow<List<MataKuliah>> = repository.getSchedulesFlow()
        .map { entities ->
            // Mapping dari ScheduleEntity (lokal) ke MataKuliah (UI Model)
            entities.map { it.toUiModel() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Inisialisasi: Panggil refresh untuk mengambil data pertama kali
    init {
        // Panggil refresh data saat ViewModel dibuat
        refreshDataFromNetwork()
    }

    // --- FUNGSI BARU UNTUK KOMUNIKASI NETWORK ---

    // Fungsi untuk menarik data terbaru dari server
    fun refreshDataFromNetwork() {
        viewModelScope.launch {
            try {
                repository.refreshSchedules()
                // Nanti: Tambahkan notifikasi sukses/error ke UI
            } catch (e: Exception) {
                // Handle error (misal: koneksi gagal)
                // Nanti: Tampilkan Toast/Snackbar error di UI
            }
        }
    }

    // --- FUNGSI SAVE BARU (Menggunakan Repository) ---

    fun saveNewSchedule(input: ScheduleInput) {
        viewModelScope.launch {
            try {
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
                // 2. Kirim ke Repository (yang akan mengirim ke Server & Refresh Room)
                repository.createSchedule(request)

            } catch (e: Exception) {
                // Handle error
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