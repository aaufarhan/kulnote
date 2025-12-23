package com.example.kulnote.data.repository

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

    fun getNotesForMatkul(matkulId: String): Flow<List<Note>> {
        return noteDao.getNotesForMatkul(matkulId)
            .map { entities ->
                entities.map { it.toUiModel() }
            }
    }

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

                    val entities = apiData.map { it.toEntity() }

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
                    ?: throw Exception("Note data is null")
                Log.d("NoteRepository", "✅ Note tersimpan di server: $savedNote")

                val entity = savedNote.toEntity()
                noteDao.insert(entity)
                Log.d("NoteRepository", "💾 Note langsung disimpan ke Room")
                
                savedNote.id
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Create Error: ${e.message}", e)
                throw e
            }
        }
    }

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

                refreshNotes(request.idJadwal)
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Update Error: ${e.message}", e)
                throw e
            }
        }
    }

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

                noteDao.deleteById(noteId)
            } catch (e: Exception) {
                Log.e("NoteRepository", "❌ Delete Error: ${e.message}", e)
                throw e
            }
        }
    }
}

fun NoteApiModel.toEntity(): NoteEntity {
    val contentJsonString = try {
        Gson().toJson(this.contentJson ?: emptyList<ContentItemJson>())
    } catch (e: Exception) {
        "[]"
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

fun NoteEntity.toUiModel(): Note {
    return Note(
        id = this.id,
        matkulId = this.idJadwal,
        title = this.judulCatatan,
        content = parseContentFromJson(this.contentJson),
        timestamp = this.timestamp
    )
}

fun parseContentFromJson(json: String?): List<NoteContentItem> {
    if (json.isNullOrBlank() || json == "[]") {
        return listOf(NoteContentItem.Text(""))
    }

    return try {
        val gson = Gson()
        val type = object : TypeToken<List<ContentItemJson>>() {}.type
        val contentJsonList: List<ContentItemJson> = gson.fromJson(json, type) ?: emptyList()

        contentJsonList.map { it.toNoteContentItem() }
    } catch (e: Exception) {
        Log.e("NoteRepository", "❌ JSON Parse Error: ${e.message}", e)
        listOf(NoteContentItem.Text(json))
    }
}

fun serializeContentToJson(content: List<NoteContentItem>): String {
    return try {
        val contentJsonList = content.map { it.toContentItemJson() }
        Gson().toJson(contentJsonList)
    } catch (e: Exception) {
        Log.e("NoteRepository", "❌ JSON Serialize Error: ${e.message}", e)
        "[]"
    }
}

fun ContentItemJson.toNoteContentItem(): NoteContentItem {
    return when (this.type.lowercase()) {
        "text" -> NoteContentItem.Text(this.text ?: "")
        "image" -> NoteContentItem.Image(
            drawableResId = this.drawableResId,
            imageUri = this.imageUri,
            widthPx = this.widthPx ?: 750,
            heightPx = this.heightPx ?: 600,
            isInline = false
        )
        "file" -> NoteContentItem.File(
            fileName = this.fileName ?: "Unknown File",
            fileUri = this.fileUri
        )
        "imagegroup" -> NoteContentItem.ImageGroup(
            imageUris = this.imageUris ?: emptyList(),
            isInline = this.isInline ?: true
        )
        else -> NoteContentItem.Text("")
    }
}

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
            widthPx = this.widthPx,
            heightPx = this.heightPx,
            isInline = false
        )
        is NoteContentItem.File -> ContentItemJson(
            type = "file",
            fileName = this.fileName,
            fileUri = this.fileUri
        )
        is NoteContentItem.ImageGroup -> ContentItemJson(
            type = "imagegroup",
            imageUris = this.imageUris,
            isInline = this.isInline
        )
    }
}

fun List<NoteContentItem>.toContentJsonList(): List<ContentItemJson> {
    return this.map { it.toContentItemJson() }
}
