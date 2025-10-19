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
fun NewNoteScreen(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel = viewModel(),
    noteViewModel: NoteViewModel = viewModel()
) {
    // Asumsi: ScheduleViewModel memiliki mataKuliahList
    val mataKuliahList by scheduleViewModel.mataKuliahList.collectAsState()
    var input by remember { mutableStateOf(NoteInput()) }
    var isExpanded by remember { mutableStateOf(false) }

    val isButtonEnabled = remember(input) {
        input.title.isNotBlank() && input.matkulId.isNotBlank()
    }

    // Hanya tampilkan placeholder jika daftar mata kuliah kosong
    if (mataKuliahList.isEmpty()) {
        // Asumsi: NoSchedulePlaceholder adalah Composable Anda
        NoSchedulePlaceholder(navController)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "New Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = input.title,
                    onValueChange = { input = input.copy(title = it) },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown Mata Kuliah
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        // Cari nama matkul berdasarkan ID yang disimpan
                        value = mataKuliahList.find { it.id == input.matkulId }?.namaMatkul ?: "Pilih Mata Kuliah",
                        onValueChange = { /* Read-only */ },
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
                                    input = input.copy(matkulId = matkul.id) // Simpan ID mata kuliah
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tombol Save
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        noteViewModel.saveNewNote(input)
                        navController.popBackStack() // Kembali ke layar sebelumnya
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Save Note")
            }
        }
    }
}

// Composable untuk menampilkan pesan jika belum ada jadwal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoSchedulePlaceholder(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Anda harus menambahkan Jadwal (Mata Kuliah) terlebih dahulu sebelum membuat catatan baru.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.navigate("add_schedule") }) {
                    Text("Tambah Jadwal Sekarang")
                }
            }
        }
    }
}