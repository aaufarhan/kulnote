package com.example.kulnote.data.model

data class MataKuliah(
    val id: String,
    val namaMatkul: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String?
)