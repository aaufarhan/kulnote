package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.model.Note
import com.example.kulnote.data.model.NoteInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID



class NoteViewModel : ViewModel() {

    // Menggunakan List<Note> (Immutable) di StateFlow untuk pemicuan pembaruan UI yang andal
    private val _noteList = MutableStateFlow(listOf<Note>())
    val noteList: StateFlow<List<Note>> = _noteList.asStateFlow()

    // FUNGSI: Logika untuk menyimpan data input catatan baru
    fun saveNewNote(input: NoteInput) {
        if (input.title.isBlank() || input.matkulId.isBlank()) return

        val newNote = Note(
            id = UUID.randomUUID().toString(),
            title = input.title,
            content = input.content,
            matkulId = input.matkulId // Gunakan ID dari input
        )

        _noteList.update { currentList ->
            // KRUSIAL: Mengembalikan List baru dengan item yang ditambahkan
            currentList + newNote
        }
    }
}

// Model Input untuk form New Note
