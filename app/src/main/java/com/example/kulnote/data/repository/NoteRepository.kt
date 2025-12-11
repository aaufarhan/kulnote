bipackage com.example.kulnote.data.repository

import android.util.Log
import com.example.kulnote.data.local.dao.NoteDao
import com.example.kulnote.data.local.model.NoteEntity
import com.example.kulnote.data.model.Note
import com.example.kulnote.data.model.NoteContentItem
import com.example.kulnote.data.model.network.ContentItemJson
import com.example.kulnote.data.model.network.NoteApiModel
import com.example.kulnote.data.model.network.NoteRequest
import com.example.kulnote.data.network.ApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class NoteRepository(
    private val apiService: ApiService,
    private val noteDao: NoteDao
) {

    // 1. READ: Flow dari Room untuk notes per matkul
    fun getNotesForMatkul(matkulId: String): Flow<List<Note>> {
        return noteDao.getNotesForMatkul(matkulId)
            .map { entities ->
                entities.map { it.toUiModel() }
            }
    }

    // 2. REFRESH: Ambil dari server, simpan ke Room
    suspend fun refreshNotes(matkulId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("NoteRepository", "📡 GET /api/notes?matkulId=$matkulId")

                val response = apiService.getNotes(matkulId).awaitResponse()

                Log.d("NoteRepository", "📥 Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val noteResponse = response.body()
                    val apiData = noteResponse?.data ?: emptyList()
                    Log.d("NoteRepository", "📊 Data diterima: ${apiData.size} notes")

                    // Konversi ApiModel ke Entity Room
                    val entities = apiData.map { it.toEntity() }

                    // REPLACE ALL: Hapus notes lama untuk matkul ini, simpan yang baru
                    noteDao.replaceAllForMatkul(matkulId, entities)
                    Log.d("NoteRepository", "💾 Disimpan ke Room: ${entities.size} notes")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NoteRepository", "❌ API Error ${response.code()}: $errorBody")
                    throw Exception("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Network Failure: ${e.message}", e)
                throw Exception("Network Failure: ${e.message}")
            }
        }
    }

    // 3. CREATE: Simpan ke server, refresh lokal
    suspend fun createNote(request: NoteRequest): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NoteRepository", "📤 POST /api/notes")
                Log.d("NoteRepository", "📦 Request: $request")

                val response = apiService.createNote(request).awaitResponse()

                Log.d("NoteRepository", "📥 Response Code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NoteRepository", "❌ Save Error ${response.code()}: $errorBody")
                    throw Exception("Gagal menyimpan Note: ${response.code()}")
                }

                val savedNote = response.body()?.data
                Log.d("NoteRepository", "✅ Note tersimpan di server: $savedNote")

                // Refresh data lokal
                Log.d("NoteRepository", "🔄 Refreshing local data...")
                refreshNotes(request.idJadwal)
                
                // Return note ID
                savedNote?.id ?: throw Exception("Note ID is null")
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Create Error: ${e.message}", e)
                throw e
            }
        }
    }

    // 4. UPDATE: Update di server, refresh lokal
    suspend fun updateNote(noteId: String, request: NoteRequest) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("NoteRepository", "📤 PUT /api/notes/$noteId")

                val response = apiService.updateNote(noteId, request).awaitResponse()

                Log.d("NoteRepository", "📥 Response Code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NoteRepository", "❌ Update Error ${response.code()}: $errorBody")
                    throw Exception("Gagal update Note: ${response.code()}")
                }

                Log.d("NoteRepository", "✅ Note berhasil diupdate")

                // Refresh data lokal
                refreshNotes(request.idJadwal)
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Update Error: ${e.message}", e)
                throw e
            }
        }
    }

    // 5. DELETE: Hapus dari server, refresh lokal
    suspend fun deleteNote(noteId: String, matkulId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("NoteRepository", "📤 DELETE /api/notes/$noteId")

                val response = apiService.deleteNote(noteId).awaitResponse()

                Log.d("NoteRepository", "📥 Response Code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("NoteRepository", "❌ Delete Error ${response.code()}: $errorBody")
                    throw Exception("Gagal hapus Note: ${response.code()}")
                }

                Log.d("NoteRepository", "✅ Note berhasil dihapus")

                // Hapus dari lokal juga
                noteDao.deleteById(noteId)
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Delete Error: ${e.message}", e)
                throw e
            }
        }
    }
}

// ========================================
// EXTENSION FUNCTIONS - MAPPING/CONVERSION
// ========================================

// Konversi NoteApiModel (dari server) ke NoteEntity (Room)
fun NoteApiModel.toEntity(): NoteEntity {
    // Serialize contentJson ke JSON string
    val contentJsonString = try {
        Gson().toJson(this.contentJson ?: emptyList<ContentItemJson>())
    } catch (e: Exception) {
        "[]" // Fallback ke empty array
    }

    return NoteEntity(
        id = this.id,
        userId = this.userId,
        idJadwal = this.idJadwal,
        judulCatatan = this.judulCatatan,
        contentJson = contentJsonString,
        timestamp = System.currentTimeMillis()
    )
}

// Konversi NoteEntity (Room) ke Note (UI Model)
fun NoteEntity.toUiModel(): Note {
    return Note(
        id = this.id,
        matkulId = this.idJadwal,
        title = this.judulCatatan,
        content = parseContentFromJson(this.contentJson),
        timestamp = this.timestamp
    )
}

// Parse JSON string ke List<NoteContentItem>
fun parseContentFromJson(json: String?): List<NoteContentItem> {
    if (json.isNullOrBlank() || json == "[]") {
        return listOf(NoteContentItem.Text(""))
    }

    return try {
        val gson = Gson()
        val type = object : TypeToken<List<ContentItemJson>>() {}.type
        val contentJsonList: List<ContentItemJson> = gson.fromJson(json, type) ?: emptyList()

        // Convert ContentItemJson to NoteContentItem
        contentJsonList.map { it.toNoteContentItem() }
    } catch (e: Exception) {
        Log.e("NoteRepository", "❌ JSON Parse Error: ${e.message}", e)
        listOf(NoteContentItem.Text(json)) // Fallback: tampilkan sebagai text
    }
}

// Serialize List<NoteContentItem> ke JSON string
fun serializeContentToJson(content: List<NoteContentItem>): String {
    return try {
        val contentJsonList = content.map { it.toContentItemJson() }
        Gson().toJson(contentJsonList)
    } catch (e: Exception) {
        Log.e("NoteRepository", "❌ JSON Serialize Error: ${e.message}", e)
        "[]" // Fallback
    }
}

// Convert ContentItemJson to NoteContentItem
fun ContentItemJson.toNoteContentItem(): NoteContentItem {
    return when (this.type.lowercase()) {
        "text" -> NoteContentItem.Text(this.text ?: "")
        "image" -> NoteContentItem.Image(
            drawableResId = this.drawableResId,
            imageUri = this.imageUri,
            widthPx = this.widthPx ?: 750,  // Default ~250dp @ 3x density (match reference)
            heightPx = this.heightPx ?: 600  // Default ~200dp @ 3x density
            // No isInline - images are always block-level (Top & Bottom)
        )
        "file" -> NoteContentItem.File(
            fileName = this.fileName ?: "Unknown File",
            fileUri = this.fileUri
        )
        "imagegroup" -> NoteContentItem.ImageGroup(
            imageUris = this.imageUris ?: emptyList(),
            isInline = this.isInline ?: true
        )
        else -> NoteContentItem.Text("") // Fallback
    }
}

// Convert NoteContentItem to ContentItemJson
fun NoteContentItem.toContentItemJson(): ContentItemJson {
    return when (this) {
        is NoteContentItem.Text -> ContentItemJson(
            type = "text",
            text = this.text
        )
        is NoteContentItem.Image -> ContentItemJson(
            type = "image",
            drawableResId = this.drawableResId,
            imageUri = this.imageUri,
            widthPx = this.widthPx,  // ✅ Include size
            heightPx = this.heightPx, // ✅ Include size
            isInline = false // Always false - images are block-level
        )
        is NoteContentItem.File -> ContentItemJson(
            type = "File",
            fileName = this.fileName,
            fileUri = this.fileUri
        )
        is NoteContentItem.ImageGroup -> ContentItemJson(
            type = "ImageGroup",
            imageUris = this.imageUris,
            isInline = this.isInline
        )
    }
}

// Convert List<NoteContentItem> to List<ContentItemJson> for API request
fun List<NoteContentItem>.toContentJsonList(): List<ContentItemJson> {
    return this.map { it.toContentItemJson() }
}
