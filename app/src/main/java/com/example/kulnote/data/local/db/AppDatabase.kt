// FILE: AppDatabase.kt

package com.example.kulnote.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kulnote.data.local.dao.NoteDao
import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.local.model.NoteEntity
import com.example.kulnote.data.local.model.ScheduleEntity

@Database(entities = [ScheduleEntity::class, NoteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun noteDao(): NoteDao

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
                )
                    .fallbackToDestructiveMigration() // Sementara untuk development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}