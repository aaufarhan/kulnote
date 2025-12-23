package com.example.kulnote.ui.screen.reminder

import android.app.TimePickerDialog
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kulnote.data.model.ReminderInput
import com.example.kulnote.data.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderForm(
    onDismiss: () -> Unit,
    viewModel: ReminderViewModel,
    initialInput: ReminderInput? = null, // Tambahkan ini
    title: String = "New Reminder",       // Tambahkan ini
    confirmLabel: String = "Save",      // Tambahkan ini
    onSubmit: (ReminderInput, Uri?) -> Unit // Ubah ini
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // State untuk input
    var subject by remember { mutableStateOf(initialInput?.subject ?: "") }
    var description by remember { mutableStateOf(initialInput?.description ?: "") }
    var selectedDate by remember { mutableStateOf(initialInput?.date ?: "") }
    var selectedTime by remember { mutableStateOf(initialInput?.time ?: "") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) {
                    selectedFileName = c.getString(nameIndex)
                }
            }
        }
    }

    // State untuk kontrol Dialog DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Logika tombol Save aktif
    val isButtonEnabled = subject.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()

    // Date Picker Dialog logic
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                    } ?: ""
                    selectedDate = date
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Input Subject
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                placeholder = { Text("What to remember?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Input Tanggal
            Box {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Overlay clickable layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            // Input Jam
            Box {
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { },
                    label = { Text("Time") },
                    readOnly = true,
                    placeholder = { Text("HH:mm") },
                    trailingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // Overlay clickable layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            TimePickerDialog(context, { _, hour, minute ->
                                selectedTime = String.format("%02d:%02d", hour, minute)
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                        }
                )
            }

            // Input Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // File Picker UI
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Lampiran File",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedFileUri == null) {
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Lampiran")
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedFileName,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { 
                                selectedFileUri = null
                                selectedFileName = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove file",
                                    modifier = Modifier.size(20.dp) // Set specific size
                                )
                            }
                        }
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
                        onSubmit(
                            ReminderInput(subject, selectedDate, selectedTime, description),
                            selectedFileUri
                        )
                        onDismiss()
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(confirmLabel)
                }
            }
        }
    }
}
