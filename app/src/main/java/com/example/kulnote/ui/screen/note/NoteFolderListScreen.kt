package com.example.kulnote.ui.screen.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kulnote.R
import com.example.kulnote.data.viewmodel.ScheduleViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteFolderListScreen(
    navController: NavController,
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val mataKuliahList by viewModel.mataKuliahList.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.onPrimary
    )
    { innerPadding ->
        if (mataKuliahList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your Desk is Empty. Add a blank notebook.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(mataKuliahList, key = { it.id }) { mataKuliah ->
                    FolderItem(
                        title = mataKuliah.namaMatkul,
                        onClick = {
                            val matkulId = mataKuliah.id
                            navController.navigate("note_list_screen/$matkulId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderItem(
    title: String,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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

    val folderIcon = if (isDarkTheme) {
        R.drawable.ic_folderdark
    } else {
        R.drawable.ic_folderlight
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scaleAnimation"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .clickable {
                    isPressed = true
                    coroutineScope.launch {
                        delay(150)
                        isPressed = false
                        onClick()
                    }
                },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = folderIcon),
                    contentDescription = "Folder Icon",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
