// FILE: ApiClient.kt

package com.example.kulnote.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // ========================================
    // 🎯 KONFIGURASI BASE URL - PILIH SALAH SATU:
    // ========================================

    // ✅ OPTION 1: UNTUK EMULATOR ANDROID STUDIO (Default - Paling Mudah)
    // 10.0.2.2 adalah IP khusus emulator yang mengarah ke localhost PC
    // Jalankan server: php artisan serve
    // const val BASE_URL = "http://10.0.2.2:8000/api/"

    // ✅ OPTION 2: UNTUK HP FISIK (Uncomment & ganti IP sesuai milik Anda!)
    // Cari IP dengan: ipconfig di CMD (cari "IPv4 Address")
    // Jalankan server: php artisan serve --host=0.0.0.0 --port=8000
    // PASTIKAN HP & PC WIFI SAMA!
    const val BASE_URL = "http://192.168.1.27:8000/api/"

    // ✅ OPTION 3: UNTUK EMULATOR GENYMOTION
    // const val BASE_URL = "http://10.0.3.2:8000/api/"

    // ✅ OPTION 4: UNTUK SERVER ONLINE (Production)
    // const val BASE_URL = "https://api.kulnote.com/api/"

    // ========================================

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
