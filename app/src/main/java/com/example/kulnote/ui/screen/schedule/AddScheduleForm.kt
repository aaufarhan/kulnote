package com.example.kulnote.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

private val daysOfWeek = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
)

private fun calculateEndTime(startTime: String, sks: String): String {
    val sksInt = sks.toIntOrNull()
    if (sksInt == null || sksInt <= 0) return ""

    if (startTime.length != 4) return ""
    val startHour = startTime.substring(0, 2).toIntOrNull()
    val startMinute = startTime.substring(2, 4).toIntOrNull()

    if (startHour == null || startMinute == null) return ""

    val durationInMinutes = sksInt * 50

    val totalStartMinutes = (startHour * 60) + startMinute
    val totalEndMinutes = totalStartMinutes + durationInMinutes
    val endHour = (totalEndMinutes / 60) % 24
    val endMinute = totalEndMinutes % 60

    val endHourStr = endHour.toString().padStart(2, '0')
    val endMinuteStr = endMinute.toString().padStart(2, '0')

    return "$endHourStr$endMinuteStr"
}


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
    initialInput: ScheduleInput = ScheduleInput(),
    title: String = "New Schedule",
    confirmLabel: String = "Save",
    onSubmit: (ScheduleInput) -> Unit
) {
    var input by remember { mutableStateOf(initialInput) }
    val isButtonEnabled = remember(input) { input.namaMatkul.isNotBlank() && input.hari.isNotBlank() && input.jamMulai.isNotBlank() && input.jamSelesai.isNotBlank() && input.sks.isNotBlank() }
    var sksText by remember { mutableStateOf(initialInput.sks) }
    var dosenText by remember { mutableStateOf(initialInput.dosen) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(initialInput) {
        input = initialInput
        sksText = initialInput.sks
        dosenText = initialInput.dosen
    }

    LaunchedEffect(input.jamMulai, sksText) {
        if (input.jamMulai.isNotBlank() && sksText.isNotBlank()) {
            val endTime = calculateEndTime(input.jamMulai, sksText)
            if (endTime.isNotBlank()) {
                input = input.copy(jamSelesai = endTime)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

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
                label = { Text("SKS*") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = input.hari,
                    onValueChange = {},
                    label = { Text("Day*") },
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
                    containerColor = MaterialTheme.colorScheme.surface
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
                    label = { Text("Start Time*") },
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
                    label = { Text("End Time*") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (isButtonEnabled) {
                            onSubmit(input)
                            onDismiss()
                        }
                    },
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(confirmLabel)
                }
            }
        }
    }
}