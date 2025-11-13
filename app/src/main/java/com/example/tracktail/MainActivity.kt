package com.example.tracktail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import com.example.tracktail.ui.theme.TrackTailTheme
import com.example.tracktail.screens.PetDataScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTailTheme {
                MainScreen()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    TrackTailTheme {
        MainScreen()
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🐾 TrackTail") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsWalk, contentDescription = "Spacer") },
                    label = { Text("Walk") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Przypomnienia") },
                    label = { Text("Reminders") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Zwierzaki") },
                    label = { Text("Pets") },
                    selected = selectedItem == 2,
                    onClick = { selectedItem = 2 },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ustawienia") },
                    label = { Text("Settings") },
                    selected = selectedItem == 3,
                    onClick = { selectedItem = 3 },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                )
            }
        },
        content = { innerPadding ->
            // Główna zawartość ekranu w zależności od wybranego przycisku
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (selectedItem) {
                    0 -> Text("Ekran Spaceru GPS", style = MaterialTheme.typography.titleMedium)
                    1 -> Text("Ekran Przypomnień", style = MaterialTheme.typography.titleMedium)
                    2 -> PetDataScreen()
                    3 -> Text("Ekran Ustawień", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    )
}
