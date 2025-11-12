package com.example.kulnote.ui.screen.note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.data.model.NoteInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteForm(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel,
    noteViewModel: NoteViewModel,
    onDismiss: () -> Unit
) {
    val mataKuliahList by scheduleViewModel.mataKuliahList.collectAsState()
    var input by remember { mutableStateOf(NoteInput()) }
    var isExpanded by remember { mutableStateOf(false) }

    val isButtonEnabled = input.title.isNotBlank() && input.matkulId.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "New Note",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (mataKuliahList.isEmpty()) {
                Text(
                    text = "Anda harus menambahkan Jadwal terlebih dahulu sebelum membuat catatan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Tutup")
                }
            } else {
                OutlinedTextField(
                    value = input.title,
                    onValueChange = { input = input.copy(title = it) },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = mataKuliahList.find { it.id == input.matkulId }?.namaMatkul
                            ?: "Course",
                        onValueChange = {},
                        label = { Text("Lecture") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        mataKuliahList.forEach { matkul ->
                            DropdownMenuItem(
                                text = { Text(matkul.namaMatkul) },
                                onClick = {
                                    input = input.copy(matkulId = matkul.id)
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newNoteId = noteViewModel.saveNewNote(input)
                            onDismiss() // Tutup dialog

                            if (newNoteId != null) {
                                // Navigasi ke editor setelah dialog ditutup
                                navController.navigate("note_content_screen/$newNoteId")
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


//@Composable
//fun NoSchedulePlaceholderContent(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.padding(16.dp)
//    ) {
//        Text(
//            text = "Anda harus menambahkan Jadwal terlebih dahulu sebelum membuat catatan baru.",
//            style = MaterialTheme.typography.titleMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        Button(
//            onClick = {
//                onDismiss() // Tutup dialog
//                navController.navigate("add_schedule") // Arahkan ke tambah jadwal
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Tambah Jadwal Sekarang")
//        }
//    }
//}