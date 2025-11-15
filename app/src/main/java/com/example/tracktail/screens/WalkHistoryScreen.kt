package com.example.tracktail.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracktail.data.models.Walk
import com.example.tracktail.data.repositories.WalkRepository
import com.example.tracktail.ui.theme.TrackTailTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WalkHistoryScreenPreview() {
    TrackTailTheme {
        WalkHistoryScreen()
    }
}
@Composable
fun WalkHistoryScreen() {
    val context = LocalContext.current
    val repository = remember { WalkRepository(context) }
    var walks by remember { mutableStateOf(repository.getAllWalks()) }
    var selectedWalk by remember { mutableStateOf<Walk?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historia spacerów",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (walks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak spacerów.\nRozpocznij pierwszy spacer!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(walks) { walk ->
                    WalkHistoryCard(
                        walk = walk,
                        onShowMap = { selectedWalk = walk },
                        onDelete = {
                            repository.deleteWalk(walk.id)
                            walks = repository.getAllWalks()
                        }
                    )
                }
            }
        }
    }

    // Dialog z mapą
    if (selectedWalk != null) {
        WalkMapDialog(
            walk = selectedWalk!!,
            onDismiss = { selectedWalk = null }
        )
    }
}

@Composable
fun WalkHistoryCard(
    walk: Walk,
    onShowMap: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormatter.format(Date(walk.startTime)),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📏 ${(walk.distance / 1000).roundToInt()} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "⏱️ ${formatDuration(walk.duration)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row {
                IconButton(onClick = onShowMap) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Pokaż mapę",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkMapDialog(
    walk: Walk,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mapa spaceru") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                MapViewComposable(
                    currentLocation = null,           // brak bieżącej lokalizacji
                    routePoints = walk.route          // ← TU JEST TRASA!
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}