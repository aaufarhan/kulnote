package com.example.kulnote.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kulnote.data.model.Note
import com.example.kulnote.data.model.NoteInput
import com.example.kulnote.data.model.NoteContentItem
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
    fun saveNewNote(input: NoteInput): String? {
        if (input.title.isBlank() || input.matkulId.isBlank()) return null

        val newNoteId = UUID.randomUUID().toString()

        val newNote = Note(
            id = newNoteId,
            title = input.title,
            content = mutableListOf(NoteContentItem.Text("")),
            matkulId = input.matkulId // Gunakan ID dari input
        )

        _noteList.update { currentList ->
            // KRUSIAL: Mengembalikan List baru dengan item yang ditambahkan
            currentList + newNote
        }

        return newNoteId

    }

    fun getNoteById(id: String): Note? {
        return _noteList.value.find { it.id == id }

    }
    fun updateNoteContent(noteId: String, newTitle: String, newContent: List<NoteContentItem>) {
        _noteList.update { currentList ->
            currentList.map { note ->
                if (note.id == noteId) {
                    note.copy(title = newTitle, content = newContent)
                } else {
                    note
                }
            }
        }
    }
}


// Model Input untuk form New Note
