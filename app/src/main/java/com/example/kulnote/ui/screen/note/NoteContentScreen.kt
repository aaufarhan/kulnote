package com.example.kulnote.ui.screen.note

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kulnote.data.model.NoteContentItem
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteContentScreen(
    navController: NavController,
    noteId: String,
    noteViewModel: NoteViewModel,
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current
    val note = remember(noteId) { noteViewModel.getNoteById(noteId) }
    var noteTitle by remember { mutableStateOf(note?.title ?: "Judul") }
    val noteContent = remember {
        mutableStateListOf<NoteContentItem>().apply {
            addAll(note?.content ?: listOf(NoteContentItem.Text("")))
        }
    }

    // MODIFIKASI 1 (Lanjutan): Logika untuk mengambil nama folder/matkul
    val mataKuliahList by scheduleViewModel.mataKuliahList.collectAsState()
    val folderName = remember(note, mataKuliahList) {
        mataKuliahList.find { it.id == note?.matkulId }?.namaMatkul ?: "Note"
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var lastFocusedTextFieldIndex by remember { mutableStateOf(0) }
    var lastTextFieldCursorPosition by remember { mutableStateOf(TextRange.Zero) }

    // State untuk menyimpan URI foto yang akan diambil
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    // Launcher untuk mengambil foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Foto berhasil diambil, tambahkan ke noteContent
            val newImage = NoteContentItem.Image(
                drawableResId = null,
                imageUri = photoUri.toString()
            )
            insertContentItem(
                noteContent,
                newImage,
                lastFocusedTextFieldIndex,
                lastTextFieldCursorPosition
            )
            showBottomSheet = false
        } else {
            // Gagal atau dibatalkan, hapus file temporary
            currentPhotoFile?.delete()
        }
        photoUri = null
        currentPhotoFile = null
    }

    // Launcher untuk request permission kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, buat file dan launch camera
            try {
                val photoFile = ImageUtils.createImageFile(context)
                currentPhotoFile = photoFile
                val uri = ImageUtils.getUriForFile(context, photoFile)
                photoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Permission denied
            // Bisa tampilkan Toast atau Snackbar
        }
    }

    //Logika Simpan Otomatis saat tombol Back ditekan
    fun saveChanges() {
        noteViewModel.updateNoteContent(noteId, noteTitle, noteContent.toList())
        navController.popBackStack()
    }

    // Intercept tombol back fisik
    BackHandler {
        saveChanges()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    // Button Back
                    IconButton(onClick = { saveChanges() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    // Pastikan warna title di TopAppBar juga mengikuti tema
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // Button Add
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                // MODIFIKASI 4: Atur warna FAB agar mengikuti tema (seperti LoginScreen)
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Item untuk Judul (UI-1)
            item {
                TextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        // MODIFIKASI 2: Jadikan judul Bold
                        fontWeight = FontWeight.Bold
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            "Judul Catatan",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    maxLines = 1
                )
                // MODIFIKASI 3: Ganti Spacer dengan Divider
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            itemsIndexed(noteContent, key = { index, item -> item.hashCode() + index }) { index, item ->
                when (item) {
                    is NoteContentItem.Text -> {
                        var textValueState by remember { mutableStateOf(TextFieldValue(item.text, TextRange(item.text.length))) }
                        TextField(
                            value = textValueState,
                            onValueChange = { newValue ->
                                textValueState = newValue
                                item.text = newValue.text // Update data model
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        lastFocusedTextFieldIndex = index
                                        lastTextFieldCursorPosition = textValueState.selection
                                    }
                                },
                            textStyle = TextStyle(fontSize = 16.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("") }
                        )
                    }
                    is NoteContentItem.Image -> {
                        // Tampilkan gambar (UI-3)
                        if (item.imageUri != null) {
                            // Gambar dari URI (foto kamera atau galeri)
                            AsyncImage(
                                model = Uri.parse(item.imageUri),
                                contentDescription = "Note Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (item.drawableResId != null) {
                            // Gambar dari drawable resource
                            Image(
                                painter = painterResource(id = item.drawableResId),
                                contentDescription = "Note Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    is NoteContentItem.File -> {
                        // Tampilkan file (UI-3)
                        FileAttachmentItem(fileName = item.fileName)
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                BottomSheetOption(
                    icon = Icons.Default.PhotoCamera,
                    text = "Add Photo"
                ) {
                    // Request permission kamera
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
                BottomSheetOption(
                    icon = Icons.Default.Image,
                    text = "Add Image"
                ) {
                    // TODO: Implementasi pilih gambar dari galeri
                }
                BottomSheetOption(
                    icon = Icons.Default.AttachFile,
                    text = "Add File"
                ) {
                    // TODO: Implementasi pilih file
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FileAttachmentItem(fileName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = "File Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = fileName,
            fontSize = 14.sp,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BottomSheetOption(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp)
    }
}


fun insertContentItem(
    contentList: SnapshotStateList<NoteContentItem>,
    newItem: NoteContentItem,
    index: Int,
    cursorPosition: TextRange
) {
    if (index < 0 || index >= contentList.size) {
        // Jika indeks tidak valid, tambahkan di akhir
        contentList.add(newItem)
        if (newItem !is NoteContentItem.Text) {
            contentList.add(NoteContentItem.Text("")) // Tambah field teks baru
        }
        return
    }

    val currentItem = contentList[index]
    if (currentItem is NoteContentItem.Text) {
        val currentText = currentItem.text
        val textBefore = currentText.substring(0, cursorPosition.start)
        val textAfter = currentText.substring(cursorPosition.start)
        contentList[index] = NoteContentItem.Text(textBefore)

        // 2. Tambahkan item baru (gambar/file)
        val newItemIndex = index + 1
        contentList.add(newItemIndex, newItem)

        // 3. Tambahkan teks "after" sebagai item baru
        if (textAfter.isNotEmpty() || newItem !is NoteContentItem.Text) {
            contentList.add(newItemIndex + 1, NoteContentItem.Text(textAfter))
        }

    } else {
        // Jika item yang difokuskan bukan teks (misal, gambar),
        contentList.add(index + 1, newItem)
        if (newItem !is NoteContentItem.Text) {
            contentList.add(index + 2, NoteContentItem.Text(""))
        }
    }
}