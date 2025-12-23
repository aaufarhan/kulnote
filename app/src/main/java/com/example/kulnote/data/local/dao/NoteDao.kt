package com.example.kulnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kulnote.data.local.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes_table WHERE idJadwal = :matkulId ORDER BY timestamp DESC")
    fun getNotesForMatkul(matkulId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes_table WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Query("DELETE FROM notes_table WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    @Query("DELETE FROM notes_table WHERE idJadwal = :matkulId")
    suspend fun deleteAllForMatkul(matkulId: String)

    @Transaction
    suspend fun replaceAllForMatkul(matkulId: String, notes: List<NoteEntity>) {
        deleteAllForMatkul(matkulId)
        if (notes.isNotEmpty()) insertAll(notes)
    }
}
