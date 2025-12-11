package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class NoteEntity(
    // ID Note (Primary Key)
    @PrimaryKey
    val id: String,

    // Foreign Key ke User
    val userId: String,

    // Foreign Key ke Schedule (matkul)
    val idJadwal: String,

    // Judul catatan
    val judulCatatan: String,

    // Content dalam format JSON string (List<ContentItemJson>)
    val contentJson: String,

    // Timestamp untuk sorting
    val timestamp: Long = System.currentTimeMillis()
)
