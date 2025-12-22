package com.example.kulnote.ui.screen.addpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.kulnote.R
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.data.viewmodel.ReminderViewModel
import com.example.kulnote.ui.screen.reminder.AddReminderForm
import com.example.kulnote.ui.screen.note.AddNoteForm
import com.example.kulnote.ui.screen.schedule.AddScheduleForm

@Composable
fun AddPageScreen(
    navController: NavController,
    viewModel: ScheduleViewModel,
    noteViewModel: NoteViewModel,
    reminderViewModel: ReminderViewModel
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create New",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Tombol Add Schedule =====
        AddPageCard(
            icon = R.drawable.ic_schedule_active,
            title = "Add Schedule"
        ) {
            showScheduleDialog = true
        }

        Spacer(modifier = Modifier.height(20.dp))

        AddPageCard(
            icon = R.drawable.ic_note_active,
            title = "Add Note"
        ) {
            showNoteDialog = true
        }

        Spacer(modifier = Modifier.height(20.dp))

        AddPageCard(
            icon = R.drawable.ic_reminder_active,
            title = "Add Reminder"
        ) {
            showReminderDialog = true
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ====== POP-UP ADD SCHEDULE ======
    if (showScheduleDialog) {
        Dialog(onDismissRequest = { showScheduleDialog = false }) {
            AddScheduleForm(
                onDismiss = { showScheduleDialog = false },
                onSubmit = { input -> viewModel.saveNewSchedule(input) }
            )
        }
    }
    // ====== POP-UP ADD NOTE ======
    if (showNoteDialog) {
        Dialog(onDismissRequest = { showNoteDialog = false }) {
            AddNoteForm(
                navController = navController,
                scheduleViewModel = viewModel,
                noteViewModel = noteViewModel,
                onDismiss = { showNoteDialog = false }
            )
        }
    }
    // ====== POP-UP ADD REMINDER ======
    if (showReminderDialog) {
        // TAMBAHKAN Dialog di sini agar konsisten dengan yang lain
        Dialog(onDismissRequest = { showReminderDialog = false }) {
            AddReminderForm(
                viewModel = reminderViewModel,
                onDismiss = { showReminderDialog = false }
            )
        }
    }
}


@Composable
fun AddPageCard(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
