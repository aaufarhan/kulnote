package com.example.kulnote.ui.screen.note

import android.net.Uri
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kulnote.data.model.NoteContentItem
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteContentScreen(
    navController: NavController,
    noteId: String,
    noteViewModel: NoteViewModel,
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Observe note dari noteList StateFlow
    val noteList by noteViewModel.noteList.collectAsState()
    val note = remember(noteId, noteList) { 
        noteList.find { it.id == noteId } 
    }
    
    // Try to get matkulId from note, if available
    val matkulId = note?.matkulId
    
    // Set current matkul ID once when we know it
    LaunchedEffect(matkulId) {
        if (matkulId != null) {
            android.util.Log.d("NoteContentScreen", "🎯 Setting matkulId: $matkulId for noteId: $noteId")
            noteViewModel.setCurrentMatkul(matkulId)
        } else {
            android.util.Log.w("NoteContentScreen", "⚠️ MatkulId is null, waiting for note data...")
        }
    }
    
    var noteTitle by remember { mutableStateOf(note?.title ?: "Judul") }
    val noteContent = remember {
        mutableStateListOf<NoteContentItem>().apply {
            addAll(note?.content ?: listOf(NoteContentItem.Text("")))
        }
    }
    
    // Update title and content when note changes
    LaunchedEffect(note) {
        note?.let {
            noteTitle = it.title
            noteContent.clear()
            noteContent.addAll(it.content)
            android.util.Log.d("NoteContentScreen", "📝 Note loaded: ${it.title} with ${it.content.size} content items")
        }
    }

    // Nama folder/matkul
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
    // State untuk upload progress
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Upload image to server first
            isUploading = true
            uploadError = null
            
            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadImage(context, photoUri!!)
                
                result.onSuccess { serverUrl ->
                    // Use server URL instead of local URI
                    val newImage = NoteContentItem.Image(
                        drawableResId = null,
                        imageUri = serverUrl
                    )
                    insertContentItem(
                        noteContent,
                        newImage,
                        lastFocusedTextFieldIndex,
                        lastTextFieldCursorPosition
                    )
                    // Persist immediately
                    noteViewModel.updateNoteContent(noteId, noteTitle, noteContent.toList())
                    isUploading = false
                    showBottomSheet = false
                }.onFailure { e ->
                    uploadError = "Upload gagal: ${e.message}"
                    isUploading = false
                    currentPhotoFile?.delete()
                }
            }
        } else {
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
            try {
                val photoFile = ImageUtils.createImageFile(context)
                currentPhotoFile = photoFile
                val uri = ImageUtils.getUriForFile(context, photoFile)
                photoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher untuk pick image dari galeri (SINGLE)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Upload image to server first
            isUploading = true
            uploadError = null
            
            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadImage(context, uri)
                
                result.onSuccess { serverUrl ->
                    // Use server URL instead of local URI
                    val newImage = NoteContentItem.Image(
                        drawableResId = null,
                        imageUri = serverUrl
                    )
                    insertContentItem(
                        noteContent,
                        newImage,
                        lastFocusedTextFieldIndex,
                        lastTextFieldCursorPosition
                    )
                    // Persist immediately
                    noteViewModel.updateNoteContent(noteId, noteTitle, noteContent.toList())
                    isUploading = false
                    showBottomSheet = false
                }.onFailure { e ->
                    uploadError = "Upload gagal: ${e.message}"
                    isUploading = false
                }
            }
        }
    }

    // Launcher untuk pick file
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Query display name safely using projection
            val fileName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown File"
                } else "Unknown File"
            } ?: "Unknown File"

            // Upload file to server first
            isUploading = true
            uploadError = null
            
            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadDocument(context, uri, fileName)
                
                result.onSuccess { serverUrl ->
                    // Use server URL instead of local URI
                    val newFile = NoteContentItem.File(
                        fileName = fileName,
                        fileUri = serverUrl
                    )
                    insertContentItem(
                        noteContent,
                        newFile,
                        lastFocusedTextFieldIndex,
                        lastTextFieldCursorPosition
                    )
                    // Persist immediately
                    noteViewModel.updateNoteContent(noteId, noteTitle, noteContent.toList())
                    isUploading = false
                    showBottomSheet = false
                }.onFailure { e ->
                    uploadError = "Upload file gagal: ${e.message}"
                    isUploading = false
                }
            }
        }
    }

    // Logika Simpan Otomatis
    fun saveChanges() {
        noteViewModel.updateNoteContent(noteId, noteTitle, noteContent.toList())
        navController.popBackStack()
    }

    BackHandler {
        saveChanges()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(folderName) },
                navigationIcon = {
                    IconButton(onClick = { saveChanges() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
            // Title
            item {
                TextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
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
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    maxLines = 1
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Content Items
            itemsIndexed(noteContent, key = { index, item -> item.hashCode() + index }) { index, item ->
                when (item) {
                    is NoteContentItem.Text -> {
                        var textValueState by remember(item) { mutableStateOf(TextFieldValue(item.text, TextRange(item.text.length))) }
                        LaunchedEffect(item.text) {
                            if (textValueState.text != item.text) {
                                textValueState = TextFieldValue(item.text, TextRange(item.text.length))
                            }
                        }
                        TextField(
                            value = textValueState,
                            onValueChange = { newValue ->
                                textValueState = newValue
                                item.text = newValue.text
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
                        if (item.imageUri != null) {
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
                            Image(
                                painter = painterResource(id = item.drawableResId),
                                contentDescription = "Note Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    is NoteContentItem.File -> {
                        FileAttachmentItem(fileName = item.fileName)
                    }
                    else -> {
                        // Handle other types (ImageGroup, etc) if any
                    }
                }
            }
        }
    }

    // Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                BottomSheetOption(
                    icon = Icons.Default.PhotoCamera,
                    text = "Take Photo"
                ) {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
                BottomSheetOption(
                    icon = Icons.Default.Image,
                    text = "Add Image"
                ) {
                    pickImageLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
                BottomSheetOption(
                    icon = Icons.Default.AttachFile,
                    text = "Attach File"
                ) {
                    pickFileLauncher.launch(arrayOf("*/*"))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    
    // Upload Progress Overlay
    if (isUploading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    "Uploading...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
    
    // Upload Error Snackbar
    uploadError?.let { error ->
        LaunchedEffect(error) {
            // Show error for 3 seconds then clear
            kotlinx.coroutines.delay(3000)
            uploadError = null
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