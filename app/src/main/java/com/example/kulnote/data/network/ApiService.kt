package com.example.kulnote.data.network

import com.example.kulnote.data.model.network.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/register")
    fun register(@Body request: Map<String, String>): Call<LoginResponse>

    @POST("auth/login")
    fun login(@Body request: Map<String, String>): Call<LoginResponse>

    @GET("schedules")
    fun getSchedules(): Call<List<ScheduleApiModel>>

    @POST("schedules")
    fun createSchedule(@Body request: ScheduleRequest): Call<ScheduleApiModel>

    @PUT("schedules/{id}")
    fun updateSchedule(
        @Path("id") scheduleId: String,
        @Body request: ScheduleRequest
    ): Call<ScheduleApiModel>

    @DELETE("schedules/{id}")
    fun deleteSchedule(@Path("id") scheduleId: String): Call<SimpleResponse>

    @GET("notes")
    fun getNotes(@Query("matkulId") matkulId: String? = null): Call<NoteListResponse>

    @GET("notes/{id}")
    fun getNoteById(@Path("id") noteId: String): Call<NoteResponse>

    @POST("notes")
    fun createNote(@Body request: NoteRequest): Call<NoteResponse>

    @PUT("notes/{id}")
    fun updateNote(@Path("id") noteId: String, @Body request: NoteRequest): Call<NoteResponse>

    @DELETE("notes/{id}")
    fun deleteNote(@Path("id") noteId: String): Call<SimpleResponse>

    @GET("reminders")
    suspend fun getReminders(): Response<ReminderResponse<List<ReminderNetworkModel>>>

    @POST("reminders")
    suspend fun createReminder(@Body request: ReminderRequest): Response<ReminderResponse<ReminderNetworkModel>>

    @PUT("reminders/{id}")
    suspend fun updateReminder(
        @Path("id") reminderId: String,
        @Body request: ReminderRequest
    ): Response<ReminderResponse<ReminderNetworkModel>>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") reminderId: String): Response<SimpleResponse>

    @GET("reminders/{id}/files")
    suspend fun getReminderFiles(@Path("id") reminderId: String): Response<ReminderResponse<List<FileApiModel>>>

    @Multipart
    @POST("files")
    suspend fun uploadFileToReminder(
        @Part file: MultipartBody.Part,
        @Part("id_reminder") idReminder: RequestBody
    ): Response<ReminderResponse<FileApiModel>>

    @Multipart
    @POST("files/upload")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("type") RequestBody: RequestBody
    ): Call<FileUploadResponse>
}
