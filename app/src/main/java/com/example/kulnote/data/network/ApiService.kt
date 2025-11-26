// FILE: ApiService.kt

package com.example.kulnote.data.network

import com.example.kulnote.data.model.network.LoginResponse
import com.example.kulnote.data.model.network.ScheduleApiModel
import com.example.kulnote.data.model.network.ScheduleRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // 1. ENDPOINT AUTENTIKASI

    @POST("auth/register")
    fun register(@Body request: Map<String, String>): Call<LoginResponse>

    @POST("auth/login")
    fun login(@Body request: Map<String, String>): Call<LoginResponse>


    // 2. ENDPOINT CRUD JADWAL KELAS

    // Mengambil semua jadwal untuk user yang terautentikasi
    // OPTION 1: Jika server return plain array (RECOMMENDED)
    @GET("schedules")
    fun getSchedules(): Call<List<ScheduleApiModel>>

    // OPTION 2: Jika server return wrapped object (Uncomment jika perlu)
    // @GET("schedules")
    // fun getSchedules(): Call<ScheduleListResponse>

    // Mengirim jadwal baru ke server
    @POST("schedules")
    fun createSchedule(@Body request: ScheduleRequest): Call<ScheduleApiModel>

    // Tambahkan GET/POST/PUT/DELETE untuk Catatan dan Reminder nanti...
}