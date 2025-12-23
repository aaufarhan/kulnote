package com.example.kulnote.ui.screen.reminder

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.kulnote.data.local.model.ReminderEntity
import com.example.kulnote.data.local.model.ReminderFileEntity
import com.example.kulnote.data.model.ReminderInput
import com.example.kulnote.data.viewmodel.ReminderViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderListScreen(
    navController: NavController,
    reminderViewModel: ReminderViewModel
) {
    val reminders by reminderViewModel.allReminders.collectAsState()
    var detailTarget by remember { mutableStateOf<ReminderEntity?>(null) }
    var editTarget by remember { mutableStateOf<ReminderEntity?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
            }
        }
    )

    LaunchedEffect(Unit) {
        reminderViewModel.fetchReminders()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada pengingat.\nData akan muncul setelah sinkronisasi.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(reminders) { reminder ->
                    val isPast = checkIfPast(reminder.waktuReminder)
                    val dimmedAmount = if (isPast) 0.5f else 1.0f

                    val files by reminderViewModel.getFilesForReminder(reminder.id).collectAsState(initial = emptyList())

                    ReminderItem(
                        reminder = reminder,
                        isPast = isPast,
                        alpha = dimmedAmount,
                        hasFiles = files.isNotEmpty()
                    ) {
                        detailTarget = reminder
                    }
                }
            }
        }
    }

    editTarget?.let { target ->
        Dialog(onDismissRequest = { editTarget = null }) {
            AddReminderForm(
                onDismiss = { editTarget = null },
                viewModel = reminderViewModel,
                initialInput = target.toReminderInput(),
                title = "Edit Pengingat",
                confirmLabel = "Update",
                onSubmit = { input, _ ->
                    reminderViewModel.updateReminder(target.id, input)
                }
            )
        }
    }

    detailTarget?.let { reminder ->
        val files by reminderViewModel.getFilesForReminder(reminder.id).collectAsState(initial = emptyList())

        ReminderDetailDialog(
            reminder = reminder,
            files = files,
            onDismiss = { detailTarget = null },
            onEdit = {
                detailTarget = null
                editTarget = reminder
            },
            onDelete = {
                detailTarget = null
                reminderViewModel.deleteReminder(reminder.id)
            }
        )
    }
}

@Composable
fun ReminderItem(
    reminder: ReminderEntity,
    isPast: Boolean,
    alpha: Float,
    hasFiles: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.judul,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPast) "Sudah Lewat: ${reminder.waktuReminder}" else "Waktu: ${reminder.waktuReminder}",
                    fontSize = 14.sp,
                    color = if (isPast) Color.Gray else MaterialTheme.colorScheme.primary
                )
            }
            if (hasFiles) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Has attachment",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Detail >", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ReminderDetailDialog(
    reminder: ReminderEntity,
    files: List<ReminderFileEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Detail Pengingat",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                DetailRow(label = "Judul", value = reminder.judul)
                DetailRow(label = "Waktu", value = reminder.waktuReminder)
                DetailRow(label = "Deskripsi", value = reminder.deskripsi ?: "Tidak ada deskripsi")

                if (files.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Lampiran (${files.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        files.forEach { fileEntity ->
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        val uri = if (fileEntity.localPath != null) {
                                            val file = File(fileEntity.localPath)
                                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        } else {
                                            Uri.parse(fileEntity.remoteUrl)
                                        }

                                        intent.setDataAndType(uri, fileEntity.tipeFile)
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fileEntity.remoteUrl))
                                        context.startActivity(browserIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fileEntity.namaFile,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (checkIfPast(reminder.waktuReminder)) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Jadwal ini sudah terlewati.",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit")
                    }

                    FilledIconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun checkIfPast(waktuReminder: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val reminderDate = sdf.parse(waktuReminder)
        val now = Date()

        reminderDate?.before(now) ?: false
    } catch (e: Exception) {
        false
    }
}

fun ReminderEntity.toReminderInput(): ReminderInput {
    val parts = waktuReminder.split(" ")
    val date = parts.getOrNull(0) ?: ""
    val fullTime = parts.getOrNull(1) ?: ""
    val time = if (fullTime.length >= 5) fullTime.substring(0, 5) else fullTime
    return ReminderInput(judul, date, time, deskripsi ?: "")
}
