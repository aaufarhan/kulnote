package com.example.kulnote.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kulnote.R
import com.example.kulnote.data.network.SessionManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("TYPE") ?: "reminder"
        val userId = intent.getStringExtra("USER_ID")
        val currentLoggedInId = SessionManager.currentUserId.value
        if (userId != currentLoggedInId) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (type) {
            "schedule" -> {
                val courseName = intent.getStringExtra("SCHEDULE_NAME") ?: "Upcoming class"
                val timeRange = intent.getStringExtra("SCHEDULE_TIME") ?: "Soon"
                val room = intent.getStringExtra("SCHEDULE_ROOM")

                val channelId = "schedule_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, "Schedule Notif", NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }

                val contentText = buildString {
                    append(timeRange)
                    if (!room.isNullOrBlank()) append(" · Room ").append(room)
                }

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("jadwal kelas mu hari ini")
                    .setContentText(contentText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$courseName — $contentText"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            }

            else -> {
                val title = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder Kulnote"
                val message = intent.getStringExtra("REMINDER_DESC") ?: "Waktunya cek tugasmu!"
                val channelId = "reminder_channel"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, "Reminder Notif", NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                }

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            }
        }
    }
}