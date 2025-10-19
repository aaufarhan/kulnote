package com.example.kulnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.ui.navigation.BottomNavBar
import com.example.kulnote.ui.screen.addpage.AddPageScreen
import com.example.kulnote.ui.screen.note.NewNoteScreen
import com.example.kulnote.ui.screen.note.NoteFolderListScreen
import com.example.kulnote.ui.screen.note.NoteListScreen
import com.example.kulnote.ui.screen.schedule.AddScheduleScreen
import com.example.kulnote.ui.theme.KulnoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KulnoteTheme {
                // Surface ini mengatur warna background di luar Scaffold/App
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KulNoteApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KulNoteApp() {
    val navController = rememberNavController()
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val noteViewModel: NoteViewModel = viewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "kulnote.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            scheduleViewModel = scheduleViewModel,
            noteViewModel = noteViewModel
        )
    }
}

// Tidak ada perubahan pada NavigationGraph, sudah benar.
@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    scheduleViewModel: ScheduleViewModel,
    noteViewModel: NoteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "note_folders",
        modifier = modifier
    ) {
        // Halaman Default (Folder List)
        composable("note_folders") {
            NoteFolderListScreen(navController, scheduleViewModel, modifier)

        }
        // Halaman Add Page
        composable("add_page") {
            AddPageScreen(navController)
        }
        // Halaman Add Schedule
        composable("add_schedule") {
            // TERUSKAN ViewModel yang sama
            AddScheduleScreen(navController, scheduleViewModel)
        }

        composable("add_note") {
            NewNoteScreen(
                navController = navController,
                scheduleViewModel = scheduleViewModel,
                noteViewModel = noteViewModel
            )
        }

        // RUTE UNTUK NOTE LIST SCREEN (Menerima Argumen ID)
        composable(
            // Mengganti {folderId} menjadi {matkulId}
            route = "note_list_screen/{matkulId}",
            arguments = listOf(
                navArgument("matkulId") { // Mengganti "folderId" menjadi "matkulId"
                    type = NavType.StringType
                    nullable = true // Penting jika Anda ingin ID opsional, tapi sebaiknya tidak
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            // Mengambil matkulId
            val matkulId = backStackEntry.arguments?.getString("matkulId") ?: ""

            NoteListScreen(
                navController = navController,
                matkulId = matkulId,
                scheduleViewModel = scheduleViewModel,
                noteViewModel = noteViewModel
            )
        }
    }
}