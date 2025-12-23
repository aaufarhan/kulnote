package com.example.kulnote.ui.screen.note

import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
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

    val noteList by noteViewModel.noteList.collectAsState()
    val note = remember(noteId, noteList) {
        noteList.find { it.id == noteId }
    }

    val matkulId = note?.matkulId

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

    LaunchedEffect(note) {
        note?.let {
            noteTitle = it.title
            noteContent.clear()
            noteContent.addAll(it.content)
            android.util.Log.d("NoteContentScreen", "📝 Note loaded: ${it.title} with ${it.content.size} content items")
        }
    }

    val mataKuliahList by scheduleViewModel.mataKuliahList.collectAsState()
    val folderName = remember(note, mataKuliahList) {
        mataKuliahList.find { it.id == note?.matkulId }?.namaMatkul ?: "Note"
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var lastFocusedTextFieldIndex by remember { mutableStateOf(0) }
    var lastTextFieldCursorPosition by remember { mutableStateOf(TextRange.Zero) }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            isUploading = true
            uploadError = null

            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadImage(context, photoUri!!)

                result.onSuccess { serverUrl ->
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

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadError = null

            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadImage(context, uri)

                result.onSuccess { serverUrl ->
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

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown File"
                } else "Unknown File"
            } ?: "Unknown File"

            isUploading = true
            uploadError = null

            noteViewModel.viewModelScope.launch {
                val result = com.example.kulnote.util.FileUploadHelper.uploadDocument(context, uri, fileName)

                result.onSuccess { serverUrl ->
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
                            var widthFraction by remember(item) { mutableStateOf(item.widthFraction.coerceIn(0.2f, 1f)) }

                            ResizableImage(
                                imageUri = item.imageUri,
                                drawableResId = item.drawableResId,
                                initialWidthFraction = widthFraction,
                                initialWidthPx = item.widthPx,
                                initialHeightPx = item.heightPx,
                                onResizeChange = { newFraction, _, _ ->
                                    widthFraction = newFraction
                                },
                                onResizeEnd = { newFraction, newWidthPx, newHeightPx ->
                                    widthFraction = newFraction
                                    noteContent[index] = item.copy(
                                        widthFraction = newFraction,
                                        widthPx = newWidthPx,
                                        heightPx = newHeightPx
                                    )
                                },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Lebar: ${(widthFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is NoteContentItem.File -> {
                            FileAttachmentItem(fileName = item.fileName)
                        }
                        else -> {
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

            uploadError?.let { error ->
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    uploadError = null
                }
            }
        }
    }
}
@Composable
fun ResizableImage(
    imageUri: String?,
    drawableResId: Int?,
    initialWidthFraction: Float,
    initialWidthPx: Int,
    initialHeightPx: Int,
    onResizeChange: (Float, Int, Int) -> Unit = { _, _, _ -> },
    onResizeEnd: (Float, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var widthFraction by remember { mutableStateOf(initialWidthFraction.coerceIn(0.2f, 1f)) }
    var widthPx by remember { mutableStateOf(initialWidthPx.coerceAtLeast(200)) }
    var heightPx by remember { mutableStateOf(initialHeightPx.coerceAtLeast(200)) }
    var aspectRatio by remember { mutableStateOf(widthPx.toFloat() / heightPx.toFloat()) }
    var imageWidthPx by remember { mutableStateOf(1f) }

    fun applyScale(scale: Float) {
        val coercedScale = scale.coerceIn(0.4f, 2.5f)
        val newWidthFraction = (widthFraction * coercedScale).coerceIn(0.2f, 1f)
        val appliedScale = if (widthFraction > 0f) newWidthFraction / widthFraction else 1f
        widthFraction = newWidthFraction
        widthPx = (widthPx * appliedScale).toInt().coerceAtLeast(120)
        heightPx = (heightPx * appliedScale).toInt().coerceAtLeast(120)
        aspectRatio = widthPx.toFloat() / heightPx.toFloat()
        onResizeChange(widthFraction, widthPx, heightPx)
    }

    fun applyWidthDelta(deltaX: Float) {
        val deltaFraction = if (imageWidthPx > 0f) deltaX / imageWidthPx else 0f
        val newWidthFraction = (widthFraction + deltaFraction).coerceIn(0.2f, 1f)
        if (newWidthFraction != widthFraction) {
            val oldFraction = widthFraction
            widthFraction = newWidthFraction
            val scaleFactor = if (widthPx > 0 && oldFraction > 0f) newWidthFraction / oldFraction else 1f
            widthPx = (widthPx * scaleFactor).toInt().coerceAtLeast(120)
            aspectRatio = widthPx.toFloat() / heightPx.toFloat()
            onResizeChange(widthFraction, widthPx, heightPx)
        }
    }

    fun applyHeightDelta(deltaY: Float) {
        val newHeightPx = (heightPx + deltaY).toInt().coerceIn(120, 4000)
        if (newHeightPx != heightPx) {
            heightPx = newHeightPx
            aspectRatio = widthPx.toFloat() / heightPx.toFloat()
            onResizeChange(widthFraction, widthPx, heightPx)
        }
    }

    fun applyCornerDelta(deltaX: Float, deltaY: Float) {
        val baseWidth = imageWidthPx.coerceAtLeast(1f)
        val scaleFromX = 1f + (deltaX / baseWidth)
        val baseHeight = baseWidth / aspectRatio
        val scaleFromY = if (baseHeight > 0f) 1f + (deltaY / baseHeight) else 1f
        val scale = ((scaleFromX + scaleFromY) / 2f).coerceIn(0.4f, 2.5f)
        applyScale(scale)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        applyScale(zoom)
                        onResizeEnd(widthFraction, widthPx, heightPx)
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .clip(RoundedCornerShape(12.dp))
                .onGloballyPositioned { coordinates ->
                    imageWidthPx = coordinates.size.width.toFloat()
                }
                .background(Color.Transparent)
        ) {
            if (imageUri != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(imageUri))
                        .crossfade(true)
                        .listener(onSuccess = { _, result ->
                            val d = result.drawable
                            val w = d.intrinsicWidth.toFloat()
                            val h = d.intrinsicHeight.toFloat()
                            val ratio = if (w > 0 && h > 0) w / h else null
                            if (ratio != null && ratio.isFinite() && ratio > 0f) {
                                aspectRatio = ratio
                            }
                        })
                        .build(),
                    contentDescription = "Note Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else if (drawableResId != null) {
                val painter = painterResource(id = drawableResId)
                val intrinsic = painter.intrinsicSize
                val ratio = if (intrinsic.width > 0 && intrinsic.height > 0) {
                    intrinsic.width / intrinsic.height
                } else 4f / 3f
                aspectRatio = ratio
                Image(
                    painter = painter,
                    contentDescription = "Note Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            )

            Box(modifier = Modifier.matchParentSize()) {
                val handleSize = 16.dp
                val cornerOffset = 6.dp

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(handleSize)
                        .offset(x = (-cornerOffset)),
                    onDrag = { delta -> applyWidthDelta(delta.x) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize kiri"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(handleSize)
                        .offset(x = cornerOffset),
                    onDrag = { delta -> applyWidthDelta(delta.x) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize kanan"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(handleSize)
                        .offset(y = (-cornerOffset)),
                    onDrag = { delta -> applyHeightDelta(delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize atas"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(handleSize)
                        .offset(y = cornerOffset),
                    onDrag = { delta -> applyHeightDelta(delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize bawah"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(handleSize)
                        .offset(x = (-cornerOffset), y = (-cornerOffset)),
                    onDrag = { delta -> applyCornerDelta(delta.x, delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize sudut"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(handleSize)
                        .offset(x = cornerOffset, y = (-cornerOffset)),
                    onDrag = { delta -> applyCornerDelta(delta.x, delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize sudut"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(handleSize)
                        .offset(x = (-cornerOffset), y = cornerOffset),
                    onDrag = { delta -> applyCornerDelta(delta.x, delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize sudut"
                )

                ResizeHandle(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(handleSize)
                        .offset(x = cornerOffset, y = cornerOffset),
                    onDrag = { delta -> applyCornerDelta(delta.x, delta.y) },
                    onDragEnd = { onResizeEnd(widthFraction, widthPx, heightPx) },
                    tooltip = "Resize sudut"
                )
            }
        }
    }
}

@Composable
private fun ResizeHandle(
    modifier: Modifier,
    onDrag: (Offset) -> Unit,
    tooltip: String,
    onDragEnd: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        onDrag(dragAmount)
                    },
                    onDragEnd = {
                        onDrag(Offset.Zero)
                        onDragEnd()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
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
        contentList.add(newItem)
        if (newItem !is NoteContentItem.Text) {
            contentList.add(NoteContentItem.Text(""))
        }
        return
    }

    val currentItem = contentList[index]
    if (currentItem is NoteContentItem.Text) {
        val currentText = currentItem.text
        val textBefore = currentText.substring(0, cursorPosition.start)
        val textAfter = currentText.substring(cursorPosition.start)
        contentList[index] = NoteContentItem.Text(textBefore)

        val newItemIndex = index + 1
        contentList.add(newItemIndex, newItem)

        if (textAfter.isNotEmpty() || newItem !is NoteContentItem.Text) {
            contentList.add(newItemIndex + 1, NoteContentItem.Text(textAfter))
        }

    } else {
        contentList.add(index + 1, newItem)
        if (newItem !is NoteContentItem.Text) {
            contentList.add(index + 2, NoteContentItem.Text(""))
        }
    }
}
