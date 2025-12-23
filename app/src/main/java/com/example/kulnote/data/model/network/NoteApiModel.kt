package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class NoteApiModel(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("id_jadwal")
    val idJadwal: String,
    @SerializedName("judul_catatan")
    val judulCatatan: String,
    @SerializedName("isi_teks")
    val isiTeks: String?,
    @SerializedName("content_json")
    val contentJson: List<ContentItemJson>?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class NoteRequest(
    @SerializedName("judul_catatan")
    val judulCatatan: String,
    @SerializedName("id_jadwal")
    val idJadwal: String,
    @SerializedName("isi_teks")
    val isiTeks: String? = null,
    @SerializedName("content_json")
    val contentJson: List<ContentItemJson>
)

data class NoteListResponse(
    val status: String,
    val data: List<NoteApiModel>
)

data class NoteResponse(
    val status: String,
    val message: String,
    val data: NoteApiModel
)

data class ContentItemJson(
    val type: String,
    val text: String? = null,
    val imageUri: String? = null,
    val drawableResId: Int? = null,
    @SerializedName("width_px")
    val widthPx: Int? = 750,
    @SerializedName("height_px")
    val heightPx: Int? = 600,
    val isInline: Boolean? = true,
    val fileName: String? = null,
    val fileUri: String? = null,
    val imageUris: List<String>? = null
)

data class SimpleResponse(
    val status: String,
    val message: String
)
