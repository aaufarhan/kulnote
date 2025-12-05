// FILE: AuthViewModel.kt

package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.network.SessionManager
import com.example.kulnote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    // Inisialisasi Repository dengan ApiService dari ApiClient
    private val repository = AuthRepository(ApiClient.apiService)

    // State untuk UI
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // NEW: expose currentUserId so other parts can observe
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Nanti: Anda harus menyimpan token ke DataStore/SharedPreferences di sini

    fun attemptLogin(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        repository.login(email, password) { response, errorMessage ->
            _isLoading.value = false
            if (response != null && response.status == "success") {
                // 1. Simpan Token
                ApiClient.authToken = response.token // Sementara simpan di ApiClient
                SessionManager.authToken = response.token

                // 1b. Simpan current user id
                val userId = response.user.id
                ApiClient.authToken = response.token
                SessionManager.setCurrentUserId(userId)
                _currentUserId.value = userId

                // 2. Update status
                _isLoggedIn.value = true

            } else {
                _error.value = errorMessage ?: response?.message ?: "Login Gagal"
            }
        }
    }

    fun attemptRegister(nama: String, email: String, password: String) {
        // Implementasi register (mirip dengan login)
        _isLoading.value = true
        _error.value = null

        repository.register(nama, email, password) { response, errorMessage ->
            _isLoading.value = false
            if (response != null && response.status == "success") {
                // 1. Simpan Token (Laravel otomatis login setelah register)
                ApiClient.authToken = response.token
                SessionManager.authToken = response.token

                // 1b. Simpan current user id
                val userId = response.user.id
                ApiClient.authToken = response.token
                SessionManager.setCurrentUserId(userId)
                _currentUserId.value = userId

                // 2. Update status
                _isLoggedIn.value = true
            } else {
                _error.value = errorMessage ?: response?.message ?: "Registrasi Gagal"
            }
        }
    }
}