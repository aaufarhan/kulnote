package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_table")
data class ScheduleEntity(
    // ID Jadwal (Primary Key)
    @PrimaryKey
    val id: String,

    // Foreign Key ke User (walaupun di local, ini bagus untuk konsistensi)
    val userId: String,

    val namaMatakuliah: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String?

    // Nanti ditambahkan: val needsSync: Boolean = false (untuk Offline-First)
)