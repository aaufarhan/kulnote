// FILE: RegisterScreen.kt

package com.example.kulnote.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kulnote.data.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController) {
    // 1. Dapatkan ViewModel
    val authViewModel: AuthViewModel = viewModel()

    // 2. Amati State dari ViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.error.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Validasi sederhana: password harus sama dan semua field terisi
    val isFormValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
            password == confirmPassword && password.length >= 8

    // Efek samping: Navigasi setelah registrasi berhasil
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Setelah registrasi, langsung pindah ke halaman utama
            navController.navigate("note_folders") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            // Input Nama
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Input Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 8 chars)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Input Konfirmasi Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Tampilkan error jika ada
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Tombol Register
            Button(
                onClick = {
                    if (isFormValid) {
                        // Panggil ViewModel untuk Registrasi
                        authViewModel.attemptRegister(name, email, password)
                    }
                },
                enabled = isFormValid && !isLoading, // Disable saat form tidak valid atau loading
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Register", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Tombol kembali ke Login
            TextButton(onClick = {
                navController.popBackStack()
            }) {
                Text("Sudah punya akun? Sign in")
            }
        }
    }
}