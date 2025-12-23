package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class FileUploadResponse(
    val status: String,
    val message: String,
    val data: FileUploadData?
)

data class FileUploadData(
    val filename: String,
    val path: String,
    val url: String,
    val type: String,
    val size: Long,
    @SerializedName("mime_type")
    val mimeType: String
)

data class FileDeleteRequest(
    val path: String
)
