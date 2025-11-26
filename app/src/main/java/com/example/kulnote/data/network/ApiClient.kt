// FILE: ApiClient.kt

package com.example.kulnote.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Ganti dengan alamat IP PC Anda jika menjalankan di emulator/HP fisik
    // Contoh: const val BASE_URL = "http://192.168.1.10:8000/api/"
    const val BASE_URL = "http://192.168.1.11:8000/api/" // 10.0.2.2 adalah localhost untuk emulator Android

    // Temporary Token (Nanti akan diganti dengan SharedPreferences/DataStore)
    var authToken: String? = null

    private val okHttpClient = OkHttpClient.Builder()
        // Interceptor Logging: Ditaruh paling awal supaya mencatat semua
        .addInterceptor(HttpLoggingInterceptor().apply {
            // PENTING: Pastikan levelnya BODY agar isi JSON terlihat
            level = HttpLoggingInterceptor.Level.BODY
        })
        // Interceptor Auth & Headers
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().apply {
                // Wajib ada untuk Laravel API agar tidak redirect ke login page HTML saat error auth
                header("Accept", "application/json")
                
                if (authToken != null) {
                    header("Authorization", "Bearer $authToken")
                }
            }
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Service yang akan dipanggil di Repository/ViewModel
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
