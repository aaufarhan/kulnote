package com.example.kulnote.data.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val matkulId: String // KRUSIAL: ID mata kuliah
)

