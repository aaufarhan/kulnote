package com.example.kulnote.data.repository

import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.model.network.LoginResponse
import com.example.kulnote.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (LoginResponse?, String?) -> Unit
    ) {
        val requestBody = mapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "password_confirmation" to password
        )

        apiService.register(requestBody).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, "Login/Registrasi Gagal: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(null, "Network Error: ${t.message}")
            }
        })
    }

    fun login(
        email: String,
        password: String,
        onResult: (LoginResponse?, String?) -> Unit
    ) {
        val requestBody = mapOf(
            "email" to email,
            "password" to password
        )

        apiService.login(requestBody).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, "Login Gagal: Cek email/password")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(null, "Network Error: ${t.message}")
            }
        })
    }
}