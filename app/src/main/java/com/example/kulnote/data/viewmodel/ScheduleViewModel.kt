package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.model.MataKuliah
import com.example.kulnote.data.model.ScheduleInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel sebagai penyimpanan sementara (In-Memory Storage)
class ScheduleViewModel : ViewModel() {

    // Penyimpanan data utama untuk daftar mata kuliah
    private val _mataKuliahList = MutableStateFlow(mutableListOf<MataKuliah>())
    val mataKuliahList: StateFlow<List<MataKuliah>> = _mataKuliahList.asStateFlow()

    // FUNGSI: Logika untuk menyimpan data input ke memori
    fun saveNewSchedule(input: ScheduleInput) {
        if (input.namaMatkul.isBlank()) return // Hindari menyimpan data kosong

        // 1. Buat ID unik sederhana
        val matkulId = input.namaMatkul.filter { it.isLetterOrDigit() }.lowercase()

        // 2. Buat objek MataKuliah baru
        val newMatkul = MataKuliah(
            id = matkulId,
            namaMatkul = input.namaMatkul,
            sks = input.sks.toIntOrNull() ?: 0,
            dosen = input.dosen
        )

        // 3. Simpan ke dalam list (In-Memory)
        _mataKuliahList.update { currentList ->
            // Tambahkan objek baru ke list
            currentList.apply { add(newMatkul) }
        }

        // Catatan: Data Schedule Kelas (hari, jam) diabaikan untuk sementara,
        // hanya MataKuliah yang ditampilkan di folder.
    }
}