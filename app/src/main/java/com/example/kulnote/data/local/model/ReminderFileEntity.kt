package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminder_files",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["idReminder"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["idReminder"])]
)
data class ReminderFileEntity(
    @PrimaryKey
    val idFile: String,
    val idReminder: String,
    val namaFile: String,
    val tipeFile: String,
    val remoteUrl: String?, // URL di server
    val localPath: String? = null // Path di penyimpanan internal aplikasi
)
