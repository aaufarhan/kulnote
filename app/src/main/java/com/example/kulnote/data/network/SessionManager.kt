package com.example.kulnote.data.network

import com.example.kulnote.data.model.network.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
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
        
        if (user != null) {
            PreferencesManager.saveUserData(user.id, user.name, user.email)
        }
    }

    var authToken: String?
        get() = ApiClient.authToken
        set(value) { 
            ApiClient.authToken = value
            if (value != null) {
                PreferencesManager.saveToken(value)
            }
        }
    
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
    
    fun clearSession() {
        _currentUser.value = null
        _currentUserId.value = null
        ApiClient.authToken = null
        PreferencesManager.clearAll()
    }
}
