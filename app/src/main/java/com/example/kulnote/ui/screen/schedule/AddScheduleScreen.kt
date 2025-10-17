package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kulnote.data.model.ScheduleInput // Model Input
import com.example.kulnote.data.viewmodel.JadwalViewModel // ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    navController: NavController,
    // Menggunakan viewModel() untuk penyimpanan In-Memory (Sesuai tugas)
    viewModel: JadwalViewModel
) {
    // State Hoisting untuk semua input data
    var input by remember { mutableStateOf(ScheduleInput()) }

    // Tombol Save hanya aktif jika field penting terisi
    val isButtonEnabled = remember(input) {
        input.namaMatkul.isNotBlank() && input.jamMulai.isNotBlank() && input.ruangan.isNotBlank()
    }

    // Tambahkan variabel untuk menampung SKS sebagai String
    var sksText by remember { mutableStateOf("") }

    // Tambahkan variabel untuk menampung Dosen
    var dosenText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Menggunakan ArrowBack untuk navigasi kembali
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp), // Padding di luar Scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Konten yang bisa di-scroll
            Column(
                modifier = Modifier
                    .weight(1f) // Memberikan berat agar bisa di-scroll
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // Padding atas

                Text(
                    text = "New Schedule",
                    style = MaterialTheme.typography.titleLarge
                )

                // --- INPUT MATA KULIAH (Lecturer) ---
                OutlinedTextField(
                    value = input.namaMatkul,
                    onValueChange = { input = input.copy(namaMatkul = it) },
                    label = { Text("Lecture") },
                    placeholder = { Text("e.g., Pemrograman Teknologi Bergerak")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosenText,
                    onValueChange = { dosenText = it; input = input.copy(dosen = it) },
                    label = { Text("Lecturer") },
                    placeholder = { Text("e.g., Ana Hubby Azira, M. Eng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sksText,
                    onValueChange = { sksText = it; input = input.copy(sks = it) },
                    label = { Text("SKS") },
                    placeholder = { Text("e.g., 2")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // --- INPUT JADWAL KELAS ---

                OutlinedTextField(
                    value = input.hari,
                    onValueChange = { input = input.copy(hari = it) },
                    label = { Text("Day") },
                    placeholder = { Text("e.g., Kamis")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = input.jamMulai,
                        onValueChange = { input = input.copy(jamMulai = it) },
                        label = { Text("Time Start") },
                        placeholder = { Text("e.g., 07:30")},
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = input.jamSelesai,
                        onValueChange = { input = input.copy(jamSelesai = it) },
                        label = { Text("Time End") },
                        placeholder = { Text("e.g., 10:00")},
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = input.gedung,
                    onValueChange = { input = input.copy(gedung = it) },
                    label = { Text("Building") },
                    placeholder = { Text("e.g., H")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = input.ruangan,
                    onValueChange = { input = input.copy(ruangan = it) },
                    label = { Text("Room") },
                    placeholder = { Text("e.g., H 110")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp)) // Padding bawah
            }

            // Tombol Save (Di luar Scroll)
                Button(
                    onClick = {
                        if (isButtonEnabled) {
                            viewModel.saveNewSchedule(input) // Menyimpan ke ViewModel yang SAMA
                            navController.navigate("note_folders") { /* ... */ }
                        }
                    },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Save Schedule")
            }
        }
    }
}