package com.example.kulnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kulnote.data.viewmodel.JadwalViewModel
import com.example.kulnote.ui.navigation.BottomNavBar
import com.example.kulnote.ui.screen.addpage.AddPageScreen
import com.example.kulnote.ui.screen.note.NoteFolderListScreen
import com.example.kulnote.ui.screen.schedule.AddScheduleScreen
import com.example.kulnote.ui.theme.KulnoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KulnoteTheme {
                KulNoteApp()
            }
        }
    }
}

@Composable
fun KulNoteApp() {
    val navController = rememberNavController()
    // Definisikan ViewModel HANYA sekali di sini
    val jadwalViewModel: JadwalViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        // Teruskan instance ViewModel ke NavigationGraph
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            jadwalViewModel = jadwalViewModel // <--- Teruskan ViewModel
        )
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    jadwalViewModel: JadwalViewModel // <--- Terima ViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "note_folders",
        modifier = modifier
    ) {
        // Halaman Default (Folder List)
        composable("note_folders") {
            // TERUSKAN ViewModel yang sama
            NoteFolderListScreen(navController, jadwalViewModel)
        }
        // Halaman Add Page
        composable("add_page") {
            AddPageScreen(navController)
        }
        // Halaman Add Schedule
        composable("add_schedule") {
            // TERUSKAN ViewModel yang sama
            AddScheduleScreen(navController, jadwalViewModel)
        }
    }
}
