package com.example.kulnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kulnote.data.local.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules_table ORDER BY hari, jamMulai ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules_table WHERE userId = :userId ORDER BY hari, jamMulai ASC")
    fun getSchedulesForUser(userId: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules_table")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(schedules: List<ScheduleEntity>) {
        deleteAll()
        if (schedules.isNotEmpty()) insertAll(schedules)
    }

    @Query("DELETE FROM schedules_table WHERE id = :scheduleId")
    suspend fun deleteById(scheduleId: String)
}