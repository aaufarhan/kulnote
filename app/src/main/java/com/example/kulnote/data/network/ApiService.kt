// FILE: ApiService.kt

package com.example.kulnote.data.network

import com.example.kulnote.data.model.network.FileDeleteRequest
import com.example.kulnote.data.model.network.FileUploadResponse
import com.example.kulnote.data.model.network.LoginResponse
import com.example.kulnote.data.model.network.NoteListResponse
import com.example.kulnote.data.model.network.NoteRequest
import com.example.kulnote.data.model.network.NoteResponse
import com.example.kulnote.data.model.network.ScheduleApiModel
import com.example.kulnote.data.model.network.ScheduleRequest
import com.example.kulnote.data.model.network.SimpleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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

    // 3. ENDPOINT CRUD NOTES

    // Mengambil semua notes untuk user (optional filter by matkulId)
    @GET("notes")
    fun getNotes(@Query("matkulId") matkulId: String? = null): Call<NoteListResponse>

    // Mengambil note berdasarkan ID
    @GET("notes/{id}")
    fun getNoteById(@Path("id") noteId: String): Call<NoteResponse>

    // Membuat note baru
    @POST("notes")
    fun createNote(@Body request: NoteRequest): Call<NoteResponse>

    // Update note
    @PUT("notes/{id}")
    fun updateNote(@Path("id") noteId: String, @Body request: NoteRequest): Call<NoteResponse>

    // Hapus note
    @DELETE("notes/{id}")
    fun deleteNote(@Path("id") noteId: String): Call<SimpleResponse>

    // === FILE UPLOAD & MANAGEMENT ===
    
    // Upload file (image/document)
    @Multipart
    @POST("files/upload")
    fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody
    ): Call<FileUploadResponse>
    
    // Delete file
    @HTTP(method = "DELETE", path = "files/delete", hasBody = true)
    fun deleteFile(@Body request: FileDeleteRequest): Call<SimpleResponse>

    // Tambahkan endpoint Reminder nanti...
}