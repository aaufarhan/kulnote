package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.network.SessionManager
import com.example.kulnote.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository(ApiClient.apiService)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    fun attemptLogin(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        repository.login(email, password) { response, errorMessage ->
            _isLoading.value = false
            if (response != null && response.status == "success") {
                ApiClient.authToken = response.token
                SessionManager.authToken = response.token

                val userData = response.user
                SessionManager.setCurrentUser(userData)
                _currentUserId.value = userData.id

                _isLoggedIn.value = true

            } else {
                _error.value = errorMessage ?: response?.message ?: "Login Gagal"
            }
        }
    }

    fun attemptRegister(nama: String, email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        repository.register(nama, email, password) { response, errorMessage ->
            _isLoading.value = false
            if (response != null && response.status == "success") {
                ApiClient.authToken = response.token
                SessionManager.authToken = response.token

                val userData = response.user
                SessionManager.setCurrentUser(userData)
                _currentUserId.value = userData.id

                _isLoggedIn.value = true
            } else {
                _error.value = errorMessage ?: response?.message ?: "Registrasi Gagal"
            }
        }
    }

    fun logout() {
        SessionManager.clearSession()
        _isLoggedIn.value = false
        _currentUserId.value = null
    }
}