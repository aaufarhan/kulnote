package com.example.kulnote.data.model

// Model Tampilan untuk daftar mata kuliah
data class MataKuliah(
    val id: String,
    val namaMatkul: String,
    val sks: Int,
    val dosen: String,
    val hari: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String
)

// Model Input akan kita gunakan langsung di ViewModel