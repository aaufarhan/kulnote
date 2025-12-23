package com.example.kulnote.data.model.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: String,
    val message: String,
    val user: UserData,
    val token: String
)

data class UserData(
    val id: String,
    val name: String,
    val email: String
)