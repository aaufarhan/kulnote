package com.example.kulnote.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.kulnote.data.local.model.ScheduleEntity
import com.example.kulnote.data.network.SessionManager
import com.example.kulnote.data.receiver.AlarmReceiver
import java.util.Calendar
import java.util.Locale

class ScheduleAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: ScheduleEntity) {
        val currentUserId = SessionManager.currentUserId.value
        if (schedule.userId != currentUserId) return

        val triggerAtMillis = computeTriggerMillis(schedule) ?: return
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TYPE", "schedule")
            putExtra("USER_ID", schedule.userId)
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_NAME", schedule.namaMatakuliah)
            putExtra(
                "SCHEDULE_TIME",
                "${schedule.jamMulai} - ${schedule.jamSelesai}"
            )
            putExtra("SCHEDULE_ROOM", schedule.ruangan)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    Log.w("ScheduleAlarm", "Exact alarm not allowed; using inexact")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ScheduleAlarm", "SecurityException when scheduling: ${e.message}")
        }
    }

    fun cancel(scheduleId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun computeTriggerMillis(schedule: ScheduleEntity): Long? {
        val dayOfWeek = mapDayOfWeek(schedule.hari) ?: return null

        val now = Calendar.getInstance()
        val todayDow = now.get(Calendar.DAY_OF_WEEK)

        // Jika hari jadwal sama dengan hari ini, kirim segera (~5 detik)
        if (dayOfWeek == todayDow) {
            return now.timeInMillis + 5_000
        }

        // Jika tidak, jadwalkan di hari tersebut (minggu ini atau berikutnya) jam 07:00
        val trigger = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (trigger.timeInMillis <= now.timeInMillis) {
            trigger.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return trigger.timeInMillis
    }

    private fun mapDayOfWeek(hari: String): Int? {
        return when (hari.lowercase(Locale.getDefault())) {
            "monday", "senin" -> Calendar.MONDAY
            "tuesday", "selasa" -> Calendar.TUESDAY
            "wednesday", "rabu" -> Calendar.WEDNESDAY
            "thursday", "kamis" -> Calendar.THURSDAY
            "friday", "jumat", "jum'at" -> Calendar.FRIDAY
            "saturday", "sabtu" -> Calendar.SATURDAY
            "sunday", "minggu", "ahad" -> Calendar.SUNDAY
            else -> null
        }
    }

    private fun parseTime(raw: String): Pair<Int, Int>? {
        val trimmed = raw.trim()

        // Format HH:mm:ss or HH:mm
        val parts = trimmed.split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            if (h != null && m != null) return h to m
        }

        // Fallback for HHmm (e.g., 0730)
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length == 4) {
            val h = digits.substring(0, 2).toIntOrNull()
            val m = digits.substring(2, 4).toIntOrNull()
            if (h != null && m != null) return h to m
        }

        return null
    }
}
