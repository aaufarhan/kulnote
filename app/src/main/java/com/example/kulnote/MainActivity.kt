package com.example.kulnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "note_folders",
        modifier = modifier
    ) {
        // Halaman Default (UI 2)
        composable("note_folders") {
            NoteFolderListScreen(navController)
        }

        // Halaman Add Page (UI 3)
        composable("add_page") {
            AddPageScreen(navController)
        }

        // Halaman Pop-up Add Schedule (UI 4)
        composable("add_schedule") {
            AddScheduleScreen(navController)
        }

        // nanti bisa lanjut nambah:
        // composable("add_note") { AddNoteScreen(navController) }
        // composable("add_reminder") { AddReminderScreen(navController) }
    }
}
