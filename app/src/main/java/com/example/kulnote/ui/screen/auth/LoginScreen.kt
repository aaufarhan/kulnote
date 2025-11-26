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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kulnote.data.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    // 1. Dapatkan ViewModel
    val authViewModel: AuthViewModel = viewModel()

    // 2. Amati State dari ViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.error.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var email by remember { mutableStateOf("") } // Ganti username menjadi email
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope() // Wajib untuk menjalankan navigasi

    // Efek samping: Navigasi setelah login berhasil
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("note_folders") {
                // Bersihkan back stack agar pengguna tidak bisa kembali ke login
                popUpTo("login") { inclusive = true }
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign in",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter email") },
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

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        // Panggil ViewModel untuk Login
                        authViewModel.attemptLogin(email, password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Sign in", color = MaterialTheme.colorScheme.surface)
                }
            }

            // Tambahkan Tombol Registrasi (Opsional)
            TextButton(onClick = {
                // Navigasi ke laman registrasi
                navController.navigate("register")
            }) {
                Text("Belum punya akun? Daftar sekarang")
            }
        }
    }
}
