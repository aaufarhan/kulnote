// FILE: AppDatabase.kt

package com.example.kulnote.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.local.model.ScheduleEntity

@Database(entities = [ScheduleEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    // Nanti ditambahkan: abstract fun noteDao(): NoteDao
    // Nanti ditambahkan: abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kulnote_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}