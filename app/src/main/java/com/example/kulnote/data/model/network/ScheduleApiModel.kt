package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

// Model untuk Data Jadwal yang diterima dari server
data class ScheduleApiModel(
    // Pastikan nama properti cocok dengan nama kolom database Laravel Anda
    val id: String,
    @SerializedName("user_id") // Nama kolom di database
    val userId: String,
    @SerializedName("nama_matakuliah")
    val namaMatakuliah: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    @SerializedName("jam_mulai")
    val jamMulai: String,
    @SerializedName("jam_selesai")
    val jamSelesai: String,
    val ruangan: String?
    // created_at dan updated_at opsional bisa ditambahkan
)

// Model untuk mengirim data Jadwal baru ke server
data class ScheduleRequest(
    @SerializedName("nama_matakuliah")
    val namaMatakuliah: String,
    val sks: Int,
    val dosen: String?,
    val hari: String,
    @SerializedName("jam_mulai")
    val jamMulai: String,
    @SerializedName("jam_selesai")
    val jamSelesai: String,
    val ruangan: String?
)