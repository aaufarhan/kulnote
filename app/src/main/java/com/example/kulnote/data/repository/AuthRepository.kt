package com.example.kulnote.data.repository

import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.model.network.LoginResponse
import com.example.kulnote.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    // Fungsi untuk Registrasi
    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (LoginResponse?, String?) -> Unit // Lambda untuk callback hasil
    ) {
        val requestBody = mapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "password_confirmation" to password // Laravel butuh ini untuk validasi 'confirmed'
        )

        apiService.register(requestBody).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    // Penanganan error validasi atau server (400, 422, 500)
                    onResult(null, "Login/Registrasi Gagal: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Penanganan error koneksi (network failure)
                onResult(null, "Network Error: ${t.message}")
            }
        })
    }

    // Fungsi untuk Login (logikanya sama dengan register, hanya berbeda endpoint)
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
                    // Penanganan error validasi atau server
                    onResult(null, "Login Gagal: Cek email/password")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(null, "Network Error: ${t.message}")
            }
        })
    }
}