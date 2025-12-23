package com.example.kulnote.data.model

data class ScheduleInput(
    val idMatkul: String = "",
    val namaMatkul: String = "",
    val sks: String = "",
    val dosen: String = "",
    val hari: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val gedung: String = "",
    val ruangan: String = ""
)