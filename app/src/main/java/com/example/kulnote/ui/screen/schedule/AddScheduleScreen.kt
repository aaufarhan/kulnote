package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kulnote.data.model.Schedule
import com.example.kulnote.data.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = ScheduleViewModel()
) {
    var courseName by remember { mutableStateOf(TextFieldValue("")) }
    var time by remember { mutableStateOf(TextFieldValue("")) }
    var room by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Schedule") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input nama mata kuliah
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Input waktu kuliah
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (e.g. 08:00 - 09:40)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Input ruangan
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Save
            Button(
                onClick = {
                    if (courseName.text.isNotBlank() && time.text.isNotBlank() && room.text.isNotBlank()) {
                        val newSchedule = Schedule(
                            id = (viewModel.schedules.size + 1),
                            courseName = courseName.text,
                            time = time.text,
                            room = room.text
                        )
                        viewModel.addSchedule(newSchedule)
                        navController.navigate("note_folders") {
                            popUpTo("note_folders") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Schedule")
            }
        }
    }
}