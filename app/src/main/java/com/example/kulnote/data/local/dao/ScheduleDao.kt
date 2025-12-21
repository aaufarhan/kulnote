// FILE: ScheduleDao.kt

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

    // Ambil semua jadwal dari lokal, di-observe oleh ViewModel
    @Query("SELECT * FROM schedules_table ORDER BY hari, jamMulai ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    // Ambil jadwal untuk user tertentu
    @Query("SELECT * FROM schedules_table WHERE userId = :userId ORDER BY hari, jamMulai ASC")
    fun getSchedulesForUser(userId: String): Flow<List<ScheduleEntity>>

    // Simpan list jadwal (menggantikan yang lama jika ada konflik ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    // New: Hapus seluruh jadwal lokal (digunakan saat sinkronisasi penuh)
    @Query("DELETE FROM schedules_table")
    suspend fun deleteAll()

    // New: Replace all in a single transaction (delete then insert)
    @Transaction
    suspend fun replaceAll(schedules: List<ScheduleEntity>) {
        deleteAll()
        if (schedules.isNotEmpty()) insertAll(schedules)
    }

    // New: Hapus jadwal berdasarkan ID
    @Query("DELETE FROM schedules_table WHERE id = :scheduleId")
    suspend fun deleteById(scheduleId: String)

    // Nanti ditambahkan: delete(), update(), dll.
}