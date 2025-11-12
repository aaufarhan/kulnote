package com.example.kulnote.ui.screen.note
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kulnote.R
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.model.Note
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    matkulId: String, // Mengganti folderId menjadi matkulId
    scheduleViewModel: ScheduleViewModel,
    noteViewModel: NoteViewModel
) {
    val mataKuliahList by scheduleViewModel.mataKuliahList.collectAsState()

    // KRUSIAL: Ambil semua catatan menggunakan StateFlow yang sudah diperbaiki
    val allNotes by noteViewModel.noteList.collectAsState()

    val currentMatkul = mataKuliahList.find { it.id == matkulId }
    val folderTitle = currentMatkul?.namaMatkul ?: "Catatan"

    // Filter catatan dari StateFlow berdasarkan ID mata kuliah
    val notes = allNotes.filter { it.matkulId == matkulId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = folderTitle, fontWeight = FontWeight.Bold) },
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
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada catatan di folder ${folderTitle}.",
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteItem(
                        title = note.title,
                        onClick = {
                            navController.navigate("note_content_screen/${note.id}")

                    })
                }
            }
        }
    }
}

// Asumsi: Composable NoteItem
@Composable
fun NoteItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick)
            .padding(15.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note), // Ganti dengan icon note yang sesuai
                contentDescription = "Note Icon",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}