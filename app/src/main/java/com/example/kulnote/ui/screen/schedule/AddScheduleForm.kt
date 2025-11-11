package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kulnote.data.model.ScheduleInput
import com.example.kulnote.data.viewmodel.ScheduleViewModel

private val daysOfWeek = listOf(
    "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
)

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += ":"
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }

            override fun transformedToOriginal(offset: Int): Int =
                when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleForm(
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel
) {
    var input by remember { mutableStateOf(ScheduleInput()) }
    val isButtonEnabled = remember(input) { input.namaMatkul.isNotBlank() }
    var sksText by remember { mutableStateOf("") }
    var dosenText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "New Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(48.dp)) // biar rata tengah
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- INPUT FIELD ---
            OutlinedTextField(
                value = input.namaMatkul,
                onValueChange = { input = input.copy(namaMatkul = it) },
                label = { Text("Course *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dosenText,
                onValueChange = { dosenText = it; input = input.copy(dosen = it) },
                label = { Text("Lecturer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sksText,
                onValueChange = { newValue ->
                    val filteredValue = newValue.filter { it.isDigit() }.take(2)
                    sksText = filteredValue
                    input = input.copy(sks = filteredValue)
                },
                label = { Text("SKS") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Dropdown Hari
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = input.hari,
                    onValueChange = {},
                    label = { Text("Day") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    daysOfWeek.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                input = input.copy(hari = selectionOption)
                                isExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // Jam Mulai & Selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = input.jamMulai,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }.take(4)
                        input = input.copy(jamMulai = filtered)
                    },
                    label = { Text("Start Time") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = TimeVisualTransformation()
                )

                OutlinedTextField(
                    value = input.jamSelesai,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }.take(4)
                        input = input.copy(jamSelesai = filtered)
                    },
                    label = { Text("End Time") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = TimeVisualTransformation()
                )
            }

            OutlinedTextField(
                value = input.ruangan,
                onValueChange = { input = input.copy(ruangan = it) },
                label = { Text("Room") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        viewModel.saveNewSchedule(input)
                        onDismiss()
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Schedule")
            }
        }
    }
}
