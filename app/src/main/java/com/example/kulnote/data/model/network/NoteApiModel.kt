package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

// Model untuk Data Note yang diterima dari server
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

// Model untuk mengirim data Note baru ke server
data class NoteRequest(
    @SerializedName("judul_catatan")
    val judulCatatan: String,
    @SerializedName("id_jadwal")
    val idJadwal: String,
    @SerializedName("isi_teks")
    val isiTeks: String? = null, // Deprecated, untuk backward compatibility
    @SerializedName("content_json")
    val contentJson: List<ContentItemJson>
)

// Wrapper response untuk list notes
data class NoteListResponse(
    val status: String,
    val data: List<NoteApiModel>
)

// Response wrapper untuk single note operation
data class NoteResponse(
    val status: String,
    val message: String,
    val data: NoteApiModel
)

// Model untuk merepresentasikan ContentItem dalam JSON
// Simplified version untuk network transfer
data class ContentItemJson(
    val type: String, // "text", "image", "file", "imageGroup"
    val text: String? = null, // Untuk type text
    val imageUri: String? = null, // Untuk type image
    val drawableResId: Int? = null, // Untuk type image (jarang digunakan di network)
    @SerializedName("width_px")
    val widthPx: Int? = 750, // Default ~250dp @ 3x density
    @SerializedName("height_px")
    val heightPx: Int? = 600, // Default ~200dp @ 3x density
    val isInline: Boolean? = true, // Untuk type image
    val fileName: String? = null, // Untuk type file
    val fileUri: String? = null, // Untuk type file
    val imageUris: List<String>? = null // Untuk type imageGroup
)

// Simple response untuk delete/success operations
data class SimpleResponse(
    val status: String,
    val message: String
)
