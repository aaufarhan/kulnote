package com.example.kulnote.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    // currentUserId yang bisa di-observe
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    fun setCurrentUserId(id: String?) {
        _currentUserId.value = id
    }

    // Simpan token juga (optional) agar satu tempat menyimpan sesi
    var authToken: String?
        get() = ApiClient.authToken
        set(value) { ApiClient.authToken = value }
}

