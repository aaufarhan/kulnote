package com.example.kulnote.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kulnote.data.local.db.AppDatabase
import com.example.kulnote.data.model.Note
import com.example.kulnote.data.model.NoteInput
import com.example.kulnote.data.model.NoteContentItem
import com.example.kulnote.data.model.network.NoteRequest
import com.example.kulnote.data.network.ApiClient
import com.example.kulnote.data.repository.NoteRepository
import com.example.kulnote.data.repository.serializeContentToJson
import com.example.kulnote.data.repository.toContentJsonList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // --- INJEKSI DEPENDENCY ---
    private val database = AppDatabase.getDatabase(application)
    private val repository = NoteRepository(
        apiService = ApiClient.apiService,
        noteDao = database.noteDao()
    )

    // --- STATE FLOW ---
    // Notes untuk matkul tertentu (akan di-set dari screen)
    private val _currentMatkulId = MutableStateFlow<String?>(null)
    
    // Notes list dari repository
    private val _noteList = MutableStateFlow(listOf<Note>())
    val noteList: StateFlow<List<Note>> = _noteList.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- FUNCTIONS ---
    
    // Set matkul ID dan load notes untuk matkul tersebut
    fun setCurrentMatkul(matkulId: String) {
        if (_currentMatkulId.value == matkulId) return // Sudah di-set
        
        _currentMatkulId.value = matkulId
        
        // Observe notes dari Room untuk matkul ini
        viewModelScope.launch {
            repository.getNotesForMatkul(matkulId).collect { notes ->
                _noteList.value = notes
                Log.d("NoteViewModel", "📊 Notes updated: ${notes.size} items")
            }
        }
        
        // Refresh dari server
        refreshNotes(matkulId)
    }

    // Refresh notes dari server
    fun refreshNotes(matkulId: String = _currentMatkulId.value ?: return) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("NoteViewModel", "🔄 Refreshing notes for matkul: $matkulId")
                
                repository.refreshNotes(matkulId)
                
                Log.d("NoteViewModel", "✅ Refresh berhasil")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "❌ Refresh gagal: ${e.message}", e)
                _error.value = "Gagal memuat catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Save new note (POST ke server)
    fun saveNewNote(input: NoteInput, onSuccess: (String) -> Unit = {}) {
        if (input.title.isBlank() || input.matkulId.isBlank()) {
            _error.value = "Judul dan mata kuliah harus diisi"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("NoteViewModel", "💾 Menyimpan note baru: ${input.title}")
                
                // Convert content to JSON
                val contentJson = listOf(NoteContentItem.Text("")).toContentJsonList()
                
                val request = NoteRequest(
                    judulCatatan = input.title,
                    idJadwal = input.matkulId,
                    contentJson = contentJson
                )
                
                val noteId = repository.createNote(request)
                
                Log.d("NoteViewModel", "✅ Note tersimpan dengan ID: $noteId")
                onSuccess(noteId)
                
            } catch (e: Exception) {
                Log.e("NoteViewModel", "❌ Gagal menyimpan note: ${e.message}", e)
                _error.value = "Gagal menyimpan catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get note by ID (dari in-memory list)
    fun getNoteById(id: String): Note? {
        return _noteList.value.find { it.id == id }
    }

    // Update note content (PUT ke server)
    fun updateNoteContent(noteId: String, newTitle: String, newContent: List<NoteContentItem>) {
        val matkulId = _currentMatkulId.value ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("NoteViewModel", "📝 Updating note: $noteId")
                
                // Convert content to JSON
                val contentJson = newContent.toContentJsonList()
                
                val request = NoteRequest(
                    judulCatatan = newTitle,
                    idJadwal = matkulId,
                    contentJson = contentJson
                )
                
                repository.updateNote(noteId, request)
                
                Log.d("NoteViewModel", "✅ Note berhasil diupdate")
                
            } catch (e: Exception) {
                Log.e("NoteViewModel", "❌ Gagal update note: ${e.message}", e)
                _error.value = "Gagal update catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete note
    fun deleteNote(noteId: String) {
        val matkulId = _currentMatkulId.value ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("NoteViewModel", "🗑️ Deleting note: $noteId")
                
                repository.deleteNote(noteId, matkulId)
                
                Log.d("NoteViewModel", "✅ Note berhasil dihapus")
                
            } catch (e: Exception) {
                Log.e("NoteViewModel", "❌ Gagal hapus note: ${e.message}", e)
                _error.value = "Gagal hapus catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}


// Model Input untuk form New Note
