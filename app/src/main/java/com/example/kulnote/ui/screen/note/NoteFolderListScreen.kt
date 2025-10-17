package com.example.kulnote.ui.screen.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel biasa
import androidx.navigation.NavController
import com.example.kulnote.R
import com.example.kulnote.data.viewmodel.JadwalViewModel // Import ViewModel
import com.example.kulnote.data.model.MataKuliah // Import Model In-Memory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteFolderListScreen(
    navController: NavController,
    // TERIMA VIEWMODEL SEBAGAI PARAMETER WAJIB
    viewModel: JadwalViewModel
) {
    val mataKuliahList by viewModel.mataKuliahList.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "kulnote.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (mataKuliahList.isEmpty()) {
            // Tampilan kosong disesuaikan dengan desain target
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                // Teks disesuaikan: "Your desk is empty. Add a blank notebook."
                Text(
                    text = "Your Desk is Empty. Add a blank notebook.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Tampilkan daftar Mata Kuliah dalam grid folder
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Iterasi melalui daftar MataKuliah
                items(mataKuliahList, key = { it.id }) { mataKuliah ->
                    FolderItem(
                        // Menggunakan namaMatkul dari Model In-Memory
                        title = mataKuliah.namaMatkul,
                        onClick = {
                            // nanti diarahkan ke halaman daftar catatan matkul itu
                            // navController.navigate("notes/${mataKuliah.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderItem(
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_folder),
            contentDescription = "Folder Icon",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 8.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}