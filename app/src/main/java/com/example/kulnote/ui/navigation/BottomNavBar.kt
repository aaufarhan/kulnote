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

    val isDarkTheme = isSystemInDarkTheme()

    val containerColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.3f)
    }

    val borderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.Black.copy(alpha = 0.15f)
    }

    val iconBackgroundSelected = if (isDarkTheme) {
        Color.White
    } else {
        Color(0xFF2C2C2C)
    }

    val iconBackgroundUnselected = if (isDarkTheme) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }

    val iconColorSelected = if (isDarkTheme) {
        Color.Black
    } else {
        Color.White
    }

    val iconColorUnselected = if (isDarkTheme) {
        Color.White.copy(alpha = 0.7f)
    } else {
        Color.Black.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
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