package com.example.kulnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.example.kulnote.data.viewmodel.AuthViewModel
import com.example.kulnote.data.model.network.UserData
import com.example.kulnote.ui.navigation.BottomNavBar
import com.example.kulnote.ui.screen.auth.LoginScreen
import com.example.kulnote.ui.screen.addpage.AddPageScreen
import com.example.kulnote.ui.screen.auth.RegisterScreen
import com.example.kulnote.ui.screen.note.NoteContentScreen
import com.example.kulnote.ui.screen.note.NoteFolderListScreen
import com.example.kulnote.ui.screen.note.NoteListScreen
import com.example.kulnote.ui.screen.reminder.ReminderListScreen
import com.example.kulnote.ui.screen.schedule.ScheduleListScreen
import com.example.kulnote.ui.theme.KulnoteTheme
import com.example.kulnote.data.network.SessionManager
import com.example.kulnote.data.network.PreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        PreferencesManager.init(this)
        SessionManager.loadSession()
        
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
    val authViewModel: AuthViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by SessionManager.currentUser.collectAsState()

    val showBottomBar = when {
        currentRoute == null -> false
        currentRoute == "login" -> false
        currentRoute == "register" -> false
        currentRoute.startsWith("note_list_screen") -> false
        currentRoute.startsWith("note_content_screen") -> false
        else -> true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (currentRoute != "login" && currentRoute != "note_content_screen/{noteId}" && currentRoute != "register") {
                TopAppBar(
                    title = {
                        Text(
                            text = "kulnote.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        ProfileMenu(
                            currentUser = currentUser,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
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
    val isLoggedIn = PreferencesManager.isLoggedIn()
    val startDestination = if (isLoggedIn) "note_folders" else "login"
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("note_folders") {
            NoteFolderListScreen(navController, scheduleViewModel, modifier)
        }

        composable("add_page") {
            AddPageScreen(navController, scheduleViewModel, noteViewModel, reminderViewModel)
        }

        composable("schedule") {
            ScheduleListScreen(navController, scheduleViewModel)
        }

        composable("reminder_list") {
            ReminderListScreen(
                navController, reminderViewModel)
        }

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
            route = "note_content_screen/{noteId}",
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")

            if (noteId != null) {
                NoteContentScreen(
                    navController = navController,
                    noteId = noteId,
                    noteViewModel = noteViewModel
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun ProfileMenu(
    currentUser: UserData?,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Profile Menu",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = currentUser?.email ?: "ica@gmail.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentUser?.name ?: "Mahasiswa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Divider()
            
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Log Out",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}
