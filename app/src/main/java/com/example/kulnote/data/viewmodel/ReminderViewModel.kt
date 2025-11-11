package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.model.Reminder
import com.example.kulnote.data.model.ReminderInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ReminderViewModel : ViewModel() {
    private val _reminderList = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderList: StateFlow<List<Reminder>> = _reminderList.asStateFlow()

    fun saveNewReminder(input: ReminderInput) {
        if (input.subject.isBlank() || input.date.isBlank() || input.time.isBlank()) return

        val newReminder = Reminder(
            id = UUID.randomUUID().toString(),
            subject = input.subject,
            date = input.date,
            time = input.time,
            description = input.description
        )

        _reminderList.update { it + newReminder }
    }
}