package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // Status respons dari server (misal: "success")
    val status: String,

    // Pesan (misal: "Login berhasil!")
    val message: String,

    // Objek user yang login
    val user: UserData,

    // Token yang WAJIB disimpan (digunakan untuk otentikasi)
    val token: String
)

data class UserData(
    val id: String,
    val name: String,
    val email: String
)