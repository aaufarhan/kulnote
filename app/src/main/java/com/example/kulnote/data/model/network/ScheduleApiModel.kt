package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class ScheduleApiModel(
    val id: String,
    @SerializedName("user_id")
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
)

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