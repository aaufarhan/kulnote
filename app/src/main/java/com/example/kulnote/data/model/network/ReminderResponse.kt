package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

// Menggunakan Generic T agar fleksibel untuk List maupun Single Object
data class ReminderResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class ReminderNetworkModel(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: Int, 
    @SerializedName("jenis_reminder") val jenisReminder: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("jam") val jam: String,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("file_url") val fileUrl: String?, // Sesuaikan dengan database server
    @SerializedName("is_completed") val isCompleted: Int = 0,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class ReminderRequest(
    @SerializedName("jenis_reminder") val jenisReminder: String,
    @SerializedName("tanggal") val tanggal: String,
    @SerializedName("jam") val jam: String,
    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("file_url") val fileUrl: String? = null
)
