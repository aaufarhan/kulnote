package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class FileApiModel(
    @SerializedName("id_file") val idFile: String,
    @SerializedName("id_catatan") val idCatatan: String?,
    @SerializedName("id_reminder") val idReminder: String?,
    @SerializedName("nama_file") val namaFile: String,
    @SerializedName("tipe_file") val tipeFile: String,
    @SerializedName("path_file") val pathFile: String,
    @SerializedName("url") val url: String
)
