package com.example.kulnote.data.network

import com.example.kulnote.data.model.network.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    // currentUserId yang bisa di-observe
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser

    fun setCurrentUserId(id: String?) {
        _currentUserId.value = id
    }

    fun setCurrentUser(user: UserData?) {
        _currentUser.value = user
        _currentUserId.value = user?.id
        
        // Simpan ke SharedPreferences
        if (user != null) {
            PreferencesManager.saveUserData(user.id, user.name, user.email)
        }
    }

    // Simpan token juga (optional) agar satu tempat menyimpan sesi
    var authToken: String?
        get() = ApiClient.authToken
        set(value) { 
            ApiClient.authToken = value
            // Simpan ke SharedPreferences
            if (value != null) {
                PreferencesManager.saveToken(value)
            }
        }
    
    // Load session dari SharedPreferences saat aplikasi dibuka
    fun loadSession() {
        val token = PreferencesManager.getToken()
        val userId = PreferencesManager.getUserId()
        val userName = PreferencesManager.getUserName()
        val userEmail = PreferencesManager.getUserEmail()
        
        if (token != null && userId != null && userName != null && userEmail != null) {
            ApiClient.authToken = token
            _currentUser.value = UserData(userId, userName, userEmail)
            _currentUserId.value = userId
        }
    }
    
    // Clear session
    fun clearSession() {
        _currentUser.value = null
        _currentUserId.value = null
        ApiClient.authToken = null
        PreferencesManager.clearAll()
    }
}
