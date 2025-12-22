package com.example.kulnote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kulnote.data.local.model.ReminderFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ReminderFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<ReminderFileEntity>)

    @Query("SELECT * FROM reminder_files WHERE idReminder = :reminderId")
    fun getFilesForReminder(reminderId: String): Flow<List<ReminderFileEntity>>

    @Query("SELECT * FROM reminder_files WHERE idReminder = :reminderId")
    suspend fun getFilesForReminderSync(reminderId: String): List<ReminderFileEntity>

    @Query("DELETE FROM reminder_files WHERE idReminder = :reminderId")
    suspend fun deleteFilesForReminder(reminderId: String)
}
