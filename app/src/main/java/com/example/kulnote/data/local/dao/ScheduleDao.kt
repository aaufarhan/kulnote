// FILE: ScheduleDao.kt

package com.example.kulnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kulnote.data.local.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    // Ambil semua jadwal dari lokal, di-observe oleh ViewModel
    @Query("SELECT * FROM schedules_table ORDER BY hari, jamMulai ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    // Simpan list jadwal (menggantikan yang lama jika ada konflik ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleEntity>)

    // Nanti ditambahkan: delete(), update(), dll.
}