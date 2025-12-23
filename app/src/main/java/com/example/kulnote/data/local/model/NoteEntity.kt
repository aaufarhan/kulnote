package com.example.kulnote.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val idJadwal: String,
    val judulCatatan: String,
    val contentJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
