package com.example.auralingo.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.auralingo.ui.live.LiveSessionScreen
import com.example.auralingo.ui.profile.ProfileScreen
import com.example.auralingo.ui.review.ReviewScreen
import com.example.auralingo.ui.theme.CardElevated
import com.example.auralingo.ui.theme.NeonViolet
import com.example.auralingo.ui.theme.ObsidianDark
import com.example.auralingo.ui.theme.SlateVioletSurface
import com.example.auralingo.ui.theme.TextMuted
import com.example.auralingo.ui.vocabulary.VocabularyScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Live : Screen("live", "Live", Icons.Default.Mic)
    object Vocabulary : Screen("vocabulary", "Vocabulary", Icons.Default.Book)
    object Review : Screen("review", "Review", Icons.Default.School)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

/**
 * Shell principale dell'applicazione con barra di navigazione inferiore Material 3 che ospita Live, Vocabulary, Review e Profile.
 */
@Composable
fun MainScreen() {
    val items = listOf(
        Screen.Live,
        Screen.Vocabulary,
        Screen.Review,
        Screen.Profile,
    )
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Live) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateVioletSurface,
                tonalElevation = 8.dp,
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
                            indicatorColor = CardElevated,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianDark),
        ) {
            when (selectedScreen) {
                is Screen.Live -> LiveSessionScreen()
                is Screen.Vocabulary -> VocabularyScreen()
                is Screen.Review -> ReviewScreen()
                is Screen.Profile -> ProfileScreen()
            }
        }
    }
}
