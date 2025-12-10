package com.example.kulnote.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kulnote.R

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            "Notes",
            R.drawable.ic_note_inactive,
            R.drawable.ic_note_active,
            "note_folders"
        ),
        BottomNavItem(
            "Schedule",
            R.drawable.ic_schedule_inactive,
            R.drawable.ic_schedule_active,
            "schedule"
        ),
        BottomNavItem(
            "Reminder",
            R.drawable.ic_reminder_inactive,
            R.drawable.ic_reminder_active,
            "reminder_list"
        ),
        BottomNavItem("Add", R.drawable.ic_add_inactive, R.drawable.ic_add_inactive, "add_page")
    )

    // Deteksi tema sistem (dark/light)
    val isDarkTheme = isSystemInDarkTheme()

    // Container background: putih blur transparan untuk dark theme, hitam blur transparan untuk light theme
    val containerColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f) // Putih transparan blur untuk tema gelap
    } else {
        Color.Black.copy(alpha = 0.3f) // Hitam transparan blur untuk tema terang
    }

    // Border color: putih transparan untuk dark theme, hitam transparan untuk light theme
    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.2f) // Putih transparan untuk tema gelap
    } else {
        Color.Black.copy(alpha = 0.15f) // Hitam transparan untuk tema terang
    }

    // Icon wrapper background: putih untuk dark theme, abu-abu gelap untuk light theme
    val iconBackgroundSelected = if (isDarkTheme) {
        Color.White // Putih solid untuk selected (dark mode)
    } else {
        Color(0xFF2C2C2C) // Gelap untuk selected (light mode)
    }

    val iconBackgroundUnselected = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f) // Putih transparan untuk unselected (dark mode)
    } else {
        Color.Black.copy(alpha = 0.1f) // Hitam transparan untuk unselected (light mode)
    }

    val iconColorSelected = if (isDarkTheme) {
        Color.Black // Icon hitam pada background putih (dark mode)
    } else {
        Color.White // Icon putih pada background gelap (light mode)
    }

    val iconColorUnselected = if (isDarkTheme) {
        Color.White.copy(alpha = 0.7f) // Icon putih transparan (dark mode)
    } else {
        Color.Black.copy(alpha = 0.6f) // Icon hitam transparan (light mode)
    }

    // Floating Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp), // Floating effect
        contentAlignment = Alignment.Center
    ) {
        // Custom container dengan full control (tidak pakai NavigationBar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(30.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            // Row untuk layout icon secara horizontal
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    val interactionSource = remember { MutableInteractionSource() }

                    // Custom clickable Box untuk setiap icon
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null // No ripple effect
                            ) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Circle background wrapper untuk icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) iconBackgroundSelected
                                    else iconBackgroundUnselected
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (selected) item.activeIcon else item.inactiveIcon
                                ),
                                contentDescription = item.name,
                                modifier = Modifier.size(26.dp),
                                tint = if (selected) iconColorSelected else iconColorUnselected
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val name: String,
    val inactiveIcon: Int,
    val activeIcon: Int,
    val route: String
)