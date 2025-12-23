package com.example.kulnote.data.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kulnote.data.local.db.AppDatabase
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kulnote.data.model.ReminderInput

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ReminderRepository(
        apiService = ApiClient.apiService,
        reminderDao = database.reminderDao(),
        reminderFileDao = database.reminderFileDao(),
        context = application
    )

    val allReminders = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchReminders()
    }

    fun fetchReminders() {
        viewModelScope.launch {
            repository.refreshReminders()
        }
    }

    fun getFilesForReminder(reminderId: String) = repository.getFilesForReminder(reminderId)

    fun saveNewReminder(input: ReminderInput, fileUri: Uri? = null) {
        viewModelScope.launch {
            repository.createReminder(input, fileUri)
        }
    }

    fun updateReminder(reminderId: String, input: ReminderInput) {
        viewModelScope.launch {
            repository.updateReminder(reminderId, input)
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
        }
    }
}
