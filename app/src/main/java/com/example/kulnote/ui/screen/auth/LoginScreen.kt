package com.example.kulnote.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign in",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter name or email") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "User Icon")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Lock Icon")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        // ✅ Setelah login sukses, pindah ke halaman utama
                        navController.navigate("note_folders") {
                            popUpTo("login") { inclusive = true } // biar gak bisa balik ke login
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Sign in", color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}
