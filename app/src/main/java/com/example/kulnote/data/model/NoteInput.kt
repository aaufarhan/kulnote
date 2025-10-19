package com.example.kulnote.data.model

data class NoteInput(
    val title: String = "",
    val content: String = "",
    val matkulId: String = "" // Menggunakan matkulId untuk konsistensi
)