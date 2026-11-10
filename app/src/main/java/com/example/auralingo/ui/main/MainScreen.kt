package com.example.auralingo.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.auralingo.ui.live.LiveSessionScreen
import com.example.auralingo.ui.review.ReviewScreen
import com.example.auralingo.ui.theme.*
import com.example.auralingo.ui.vocabulary.VocabularyScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Live : Screen("live", "Live", Icons.Default.Mic)
    object Vocabulary : Screen("vocabulary", "Vocabulary", Icons.Default.Book)
    object Review : Screen("review", "Review", Icons.Default.School)
}

/**
 * Main Shell containing modern Material 3 Bottom Navigation bar for AuraLingo.
 */
@Composable
fun MainScreen() {
    val items = listOf(
        Screen.Live,
        Screen.Vocabulary,
        Screen.Review
    )
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Live) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateVioletSurface,
                tonalElevation = 8.dp
            ) {
                items.forEach { screen ->
                    val selected = selectedScreen == screen
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = { selectedScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonViolet,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonViolet,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CardElevated
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianDark)
        ) {
            when (selectedScreen) {
                is Screen.Live -> LiveSessionScreen()
                is Screen.Vocabulary -> VocabularyScreen()
                is Screen.Review -> ReviewScreen()
            }
        }
    }
}
