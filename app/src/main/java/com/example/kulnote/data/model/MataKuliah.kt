package com.example.kulnote.data.model

// Model yang digunakan oleh UI (UI Model)
// Ini adalah representasi data Jadwal Kelas yang lengkap dan bersih
data class MataKuliah(
    val id: String, // ID unik dari server (Primary Key)
    val namaMatkul: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String?
)