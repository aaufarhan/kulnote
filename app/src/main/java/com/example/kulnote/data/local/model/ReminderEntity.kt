package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders_table")
data class ReminderEntity(
    @PrimaryKey
    val id: String,

    val userId: String,

    val judul: String,

    val deskripsi: String?,

    val waktuReminder: String,

    val isCompleted: Boolean = false,

    val fileUrl: String? = null,

    val createdAt: String?,
    val updatedAt: String?
)
