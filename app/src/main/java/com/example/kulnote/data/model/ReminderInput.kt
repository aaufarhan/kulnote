package com.example.kulnote.data.model

data class ReminderInput(
    val subject: String,
    val date: String,
    val time: String,
    val description: String?
)
