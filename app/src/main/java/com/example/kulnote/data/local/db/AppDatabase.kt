package com.example.kulnote.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kulnote.data.local.dao.NoteDao
import com.example.kulnote.data.local.dao.ScheduleDao
import com.example.kulnote.data.local.model.NoteEntity
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.local.dao.ReminderDao
import com.example.kulnote.data.local.model.ReminderEntity
import com.example.kulnote.data.local.dao.ReminderFileDao
import com.example.kulnote.data.local.model.ReminderFileEntity

@Database(
    entities = [ScheduleEntity::class, NoteEntity::class, ReminderEntity::class, ReminderFileEntity::class], 
    version = 8, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderFileDao(): ReminderFileDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
