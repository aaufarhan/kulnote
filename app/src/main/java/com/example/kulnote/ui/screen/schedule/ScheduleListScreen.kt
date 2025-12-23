package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kulnote.data.model.MataKuliah
import com.example.kulnote.data.model.ScheduleInput
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import androidx.compose.ui.window.Dialog

private fun formatTime(time: String): String {
    if (time.length != 4) return time
    return try {
        val hour = time.substring(0, 2).toInt()
        val minute = time.substring(2, 4)
        "$hour.$minute"
    } catch (e: Exception) {
        time
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    navController: NavController,
    viewModel: ScheduleViewModel
) {
    val scheduleList by viewModel.mataKuliahList.collectAsState()
    var editTarget by remember { mutableStateOf<MataKuliah?>(null) }
    var detailTarget by remember { mutableStateOf<MataKuliah?>(null) }

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
                groupedSchedules.forEach { (day, schedules) ->
                    item {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Divider()
                    }

                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleItemCard(
                            schedule = schedule,
                            onClick = { detailTarget = schedule },
                            onDelete = { viewModel.deleteSchedule(schedule.id) },
                            onEdit = { editTarget = schedule }
                        )
                    }
                }
            }
        }
    }

    editTarget?.let { target ->
        Dialog(onDismissRequest = { editTarget = null }) {
            AddScheduleForm(
                onDismiss = { editTarget = null },
                initialInput = target.toScheduleInput(),
                title = "Edit Schedule",
                confirmLabel = "Update",
                onSubmit = { input ->
                    viewModel.updateSchedule(target.id, input)
                }
            )
        }
    }

    detailTarget?.let { target ->
        ScheduleDetailDialog(
            schedule = target,
            onDismiss = { detailTarget = null },
            onOpenNotebook = {
                detailTarget = null
                navController.navigate("note_list_screen/${target.id}")
            },
            onEdit = {
                detailTarget = null
                editTarget = target
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    schedule: MataKuliah,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val timeString = "${formatTime(schedule.jamMulai)} - ${formatTime(schedule.jamSelesai)} WIB"
    val detailString = if (!schedule.ruangan.isNullOrBlank()) {
        "$timeString, ${schedule.ruangan}"
    } else {
        timeString
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
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
                    tint = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.namaMatkul,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detailString,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun MataKuliah.toScheduleInput(): ScheduleInput {
    return ScheduleInput(
        idMatkul = this.id,
        namaMatkul = this.namaMatkul,
        sks = this.sks.toString(),
        dosen = this.dosen.orEmpty(),
        hari = this.hari,
        jamMulai = this.jamMulai.filter { it.isDigit() },
        jamSelesai = this.jamSelesai.filter { it.isDigit() },
        ruangan = this.ruangan.orEmpty()
    )
}

@Composable
private fun ScheduleDetailDialog(
    schedule: MataKuliah,
    onDismiss: () -> Unit,
    onOpenNotebook: () -> Unit,
    onEdit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 10.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Lecture",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = schedule.namaMatkul,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                DetailItem(label = "Time", value = "${formatTime(schedule.jamMulai)} - ${formatTime(schedule.jamSelesai)} WIB")
                DetailItem(label = "SKS", value = schedule.sks.toString())
                DetailItem(label = "Room", value = schedule.ruangan.orEmpty())
                DetailItem(label = "Lecturer", value = schedule.dosen.orEmpty())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onOpenNotebook, modifier = Modifier.weight(1f)) {
                        Text("Open Notebook")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FilledIconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit schedule"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (value.isNotBlank()) value else "-",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
