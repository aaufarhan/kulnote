package com.example.kulnote.data.model

// Data Class untuk menampung semua input dari form Add Schedule
data class ScheduleInput(
    // Properti untuk MataKuliahEntity
    val idMatkul: String = "", // Jika Anda menggunakan ID manual
    val namaMatkul: String = "",
    val sks: String = "",
    val dosen: String = "",

    // Properti untuk JadwalKelasEntity
    val hari: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val gedung: String = "",
    val ruangan: String = ""
)