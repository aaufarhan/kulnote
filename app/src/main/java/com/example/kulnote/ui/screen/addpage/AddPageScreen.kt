package com.example.kulnote.ui.screen.addpage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kulnote.R

@Composable
fun AddPageScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create New",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Add Schedule
        AddPageButton(
            icon = R.drawable.ic_schedule_active,
            title = "Add Schedule"
        ) {
            navController.navigate("add_schedule")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Add Note
        AddPageButton(
            icon = R.drawable.ic_note_active,
            title = "Add Note"
        ) {
            // nanti diarahkan ke AddNoteScreen
            // navController.navigate("add_note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Add Reminder
        AddPageButton(
            icon = R.drawable.ic_reminder_active,
            title = "Add Reminder"
        ) {
            // nanti diarahkan ke AddReminderScreen
            // navController.navigate("add_reminder")
        }
    }
}

@Composable
fun AddPageButton(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}