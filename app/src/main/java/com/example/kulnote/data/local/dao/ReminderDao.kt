package com.example.kulnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kulnote.data.local.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders_table WHERE userId = :userId ORDER BY waktuReminder ASC")
    fun getRemindersByUser(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders_table WHERE userId = :userId AND isCompleted = 0 ORDER BY waktuReminder ASC")
    fun getActiveRemindersByUser(userId: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders_table SET isCompleted = :isCompleted WHERE id = :reminderId")
    suspend fun updateCompletionStatus(reminderId: String, isCompleted: Boolean)

    @Query("DELETE FROM reminders_table WHERE id = :id")
    suspend fun deleteById(id: String)
}
