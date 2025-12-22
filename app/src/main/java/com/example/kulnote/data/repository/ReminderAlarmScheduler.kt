package com.example.kulnote.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.kulnote.data.local.model.ReminderEntity
import com.example.kulnote.data.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class ReminderAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", reminder.judul)
            putExtra("REMINDER_DESC", reminder.deskripsi)
            putExtra("USER_ID", reminder.userId) // TAMBAHKAN INI
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timeInMillis = sdf.parse(reminder.waktuReminder)?.time ?: return

        if (timeInMillis > System.currentTimeMillis()) {
            try {
                // Pengecekan untuk Android 12+ (API 31)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            pendingIntent
                        )
                    } else {
                        // Jika tidak boleh exact, gunakan setAndAllowWhileIdle (bisa meleset dikit tapi aman)
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeInMillis,
                            pendingIntent
                        )
                        Log.w("AlarmScheduler", "Exact alarm not allowed, using inexact.")
                    }
                } else {
                    // Untuk Android lama (di bawah 12)
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e("AlarmScheduler", "SecurityException: ${e.message}")
            }
        }
    }
}