package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kulnote.data.model.MataKuliah
import com.example.kulnote.data.viewmodel.ScheduleViewModel

private fun formatTime(time: String): String {
    if (time.length != 4) return time
    return try {
        val hour = time.substring(0, 2).toInt()
        val minute = time.substring(2, 4)
        "$hour.$minute"
    } catch (e: Exception) {
        time // fallback jika format salah
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    navController: NavController,
    viewModel: ScheduleViewModel
) {
    val scheduleList by viewModel.mataKuliahList.collectAsState()

    // Kelompokkan jadwal berdasarkan hari (sesuai UI)
    val groupedSchedules = scheduleList.groupBy { it.hari }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->

        if (scheduleList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No schedules added yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Iterasi setiap grup (setiap hari)
                groupedSchedules.forEach { (day, schedules) ->
                    // Header untuk hari (misal, "Thursday")
                    item {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface // Pastikan teks terlihat
                        )
                        // Garis pemisah seperti di UI
                        Divider()
                    }

                    // Daftar jadwal di hari tersebut
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItemCard(schedule = schedule)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCard(schedule: MataKuliah) {
    // Format string waktu dan ruangan
    val timeString = "${formatTime(schedule.jamMulai)} - ${formatTime(schedule.jamSelesai)} WIB"
    val detailString = if (schedule.ruangan.isNotBlank()) {
        "$timeString, ${schedule.ruangan}"
    } else {
        timeString
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Beri jarak antar card
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Gunakan surface agar warnanya konsisten (Putih/Hitam)
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        // Tambahkan border tipis seperti di UI
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Schedule Icon",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface // Pastikan ikon terlihat
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.namaMatkul,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface // Pastikan teks terlihat
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detailString,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Warna lebih redup
                )
            }
        }
    }
}