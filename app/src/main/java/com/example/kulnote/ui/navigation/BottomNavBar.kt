package com.example.kulnote.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kulnote.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Notes", R.drawable.ic_note_inactive, R.drawable.ic_note_active, "note_folders"),
        BottomNavItem("Schedule", R.drawable.ic_schedule_inactive, R.drawable.ic_schedule_active, "schedule"),
        BottomNavItem("Reminder", R.drawable.ic_reminder_inactive, R.drawable.ic_reminder_active, "reminder_list"),
        BottomNavItem("Add", R.drawable.ic_add_inactive, R.drawable.ic_add_inactive, "add_page") // pakai Material Icon bawaan
    )

    NavigationBar(
        modifier = Modifier.height(56.dp),
        tonalElevation = 4.dp,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                modifier = Modifier.padding(vertical = 4.dp),
                icon = {
//                    if (item.name == "Add") {
//                        Icon(
//                            imageVector = if (selected) Icons.Filled.Add else Icons.Outlined.Add,
//                            contentDescription = item.name
//                        )
//                    } else {
                        Icon(
                            painter = painterResource(
                                id = if (selected) item.activeIcon else item.inactiveIcon
                            ),
                            contentDescription = item.name
                        )

                },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.onBackground,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary, // Ikon terpilih
                    unselectedIconColor = MaterialTheme.colorScheme.secondary, // Ikon tidak terpilih
                )
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val inactiveIcon: Int,
    val activeIcon: Int,
    val route: String
)