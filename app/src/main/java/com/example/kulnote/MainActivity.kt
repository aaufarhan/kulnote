package com.example.kulnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kulnote.data.viewmodel.NoteViewModel
import com.example.kulnote.data.viewmodel.ReminderViewModel
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import com.example.kulnote.ui.navigation.BottomNavBar
import com.example.kulnote.ui.screen.auth.LoginScreen
import com.example.kulnote.ui.screen.addpage.AddPageScreen
import com.example.kulnote.ui.screen.note.NoteContentScreen
import com.example.kulnote.ui.screen.note.NoteFolderListScreen
import com.example.kulnote.ui.screen.note.NoteListScreen
import com.example.kulnote.ui.screen.reminder.ReminderListScreen
import com.example.kulnote.ui.screen.schedule.ScheduleListScreen
import com.example.kulnote.ui.theme.KulnoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KulnoteTheme {
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
    val reminderViewModel: ReminderViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute !in listOf("login")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (currentRoute != "login" && currentRoute != "note_content_screen/{noteId}") {
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
            }
        },
        bottomBar = {
            if (showBottomBar) BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            scheduleViewModel = scheduleViewModel,
            noteViewModel = noteViewModel,
            reminderViewModel = reminderViewModel
        )
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    scheduleViewModel: ScheduleViewModel,
    noteViewModel: NoteViewModel,
    reminderViewModel: ReminderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {

        // Halaman Login
        composable("login") {
            LoginScreen(navController)
        }

        // Halaman Default (Folder List)
        composable("note_folders") {
            NoteFolderListScreen(navController, scheduleViewModel, modifier)
        }

        // Halaman Add
        composable("add_page") {
            AddPageScreen(navController, scheduleViewModel, noteViewModel, reminderViewModel)
        }

        //Halaman Schedule
        composable("schedule") {
            ScheduleListScreen(navController, scheduleViewModel)
        }

        // Halaman Reminder
        composable("reminder_list") {
            ReminderListScreen(
                navController, reminderViewModel)
        }

        // Halaman Note List (dengan argumen ID matkul)
        composable(
            route = "note_list_screen/{matkulId}",
            arguments = listOf(
                navArgument("matkulId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val matkulId = backStackEntry.arguments?.getString("matkulId") ?: ""
            NoteListScreen(
                navController = navController,
                matkulId = matkulId,
                scheduleViewModel = scheduleViewModel,
                noteViewModel = noteViewModel
            )
        }
        composable(
            route = "note_content_screen/{noteId}", // Rute baru dengan argumen noteId
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType } // Definisikan tipe argumen
            )
        ) { backStackEntry ->
            // Ambil noteId dari argumen navigasi
            val noteId = backStackEntry.arguments?.getString("noteId")

            if (noteId != null) {
                // Tampilkan NoteContentScreen jika noteId ada
                NoteContentScreen(
                    navController = navController,
                    noteId = noteId,
                    noteViewModel = noteViewModel
                )
            } else {
                // Jika karena alasan tertentu noteId null, kembali ke layar sebelumnya
                navController.popBackStack()
            }
        }
    }
}