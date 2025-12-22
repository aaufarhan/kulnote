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

    // Waktu yang ditentukan untuk pengingat (penting untuk notifikasi)
    // Disarankan menggunakan format ISO 8601 String (YYYY-MM-DDTHH:MM:SS)
    val waktuReminder: String,

    // Status penyelesaian task
    val isCompleted: Boolean = false,

    // URL file sisipan jika ada
    val fileUrl: String? = null,

    val createdAt: String?,
    val updatedAt: String?
)
