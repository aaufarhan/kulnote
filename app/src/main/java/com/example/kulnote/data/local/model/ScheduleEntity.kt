package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules_table")
data class ScheduleEntity(
    @PrimaryKey
    val id: String,
    val userId: String,

    val namaMatakuliah: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    val jamMulai: String,
    val jamSelesai: String,
    val ruangan: String?
)