package com.example.kulnote.data.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val matkulId: String,
    val title: String,
    var content: List<NoteContentItem>,
    var timestamp: Long = System.currentTimeMillis()
)

