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
    private val database = AppDatabase.getDatabase(application)
    private val repository = NoteRepository(
        apiService = ApiClient.apiService,
        noteDao = database.noteDao()
    )

    private val _currentMatkulId = MutableStateFlow<String?>(null)
    
    private val _noteList = MutableStateFlow(listOf<Note>())
    val noteList: StateFlow<List<Note>> = _noteList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var flowCollectionJob: kotlinx.coroutines.Job? = null

    fun setCurrentMatkul(matkulId: String) {
        if (_currentMatkulId.value == matkulId) {
            Log.d("NoteViewModel", "⚠️ MatkulId sudah sama, skip re-initialization")
            return
        }
        
        Log.d("NoteViewModel", "🎯 Setting current matkulId: $matkulId")
        _currentMatkulId.value = matkulId

        flowCollectionJob?.cancel()

        flowCollectionJob = viewModelScope.launch {
            Log.d("NoteViewModel", "👀 Starting to observe notes for matkulId: $matkulId")
            repository.getNotesForMatkul(matkulId).collect { notes ->
                _noteList.value = notes
                Log.d("NoteViewModel", "📊 Notes updated: ${notes.size} items")
            }
        }

        refreshNotes(matkulId)
    }

    fun refreshNotes(matkulId: String? = null) {
        val targetMatkulId = matkulId ?: _currentMatkulId.value
        if (targetMatkulId == null) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("NoteViewModel", "🔄 Refreshing notes for matkul: $targetMatkulId")
                
                repository.refreshNotes(targetMatkulId)
                
                Log.d("NoteViewModel", "✅ Refresh berhasil")
            } catch (e: Exception) {
                Log.e("NoteViewModel", "❌ Refresh gagal: ${e.message}", e)
                _error.value = "Gagal memuat catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun getNoteById(id: String): Note? {
        return _noteList.value.find { it.id == id }
    }

    fun updateNoteContent(noteId: String, newTitle: String, newContent: List<NoteContentItem>) {
        val matkulId = _currentMatkulId.value ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("NoteViewModel", "📝 Updating note: $noteId")
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
