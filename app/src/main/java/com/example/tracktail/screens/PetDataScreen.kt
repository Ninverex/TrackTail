package com.example.tracktail.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracktail.data.models.Pet
import com.example.tracktail.data.repositories.PetRepository
import com.example.tracktail.ui.theme.TrackTailTheme
import java.text.SimpleDateFormat
import java.util.*

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PetDataScreenPreview() {
    TrackTailTheme {
        PetDataScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDataScreen() {
    val context = LocalContext.current
    val repository = remember { PetRepository(context) }

    var pets by remember { mutableStateOf(repository.getAllPets()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPet by remember { mutableStateOf<Pet?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moje zwierzaki",
                style = MaterialTheme.typography.headlineMedium
            )

            FloatingActionButton(
                onClick = {
                    editingPet = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zwierzaka")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak zwierząt.\nDodaj swojego pierwszego pupila!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pets) { pet ->
                    PetCard(
                        pet = pet,
                        onEdit = {
                            editingPet = pet
                            showAddDialog = true
                        },
                        onDelete = {
                            repository.deletePet(pet.id)
                            pets = repository.getAllPets()
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        PetDialog(
            pet = editingPet,
            onDismiss = { showAddDialog = false },
            onSave = { pet ->
                repository.savePet(pet)
                pets = repository.getAllPets()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PetCard(
    pet: Pet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp
                )
                Text(
                    text = "${pet.species} • ${pet.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Urodzony: ${dateFormatter.format(Date(pet.birthDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edytuj",
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
fun PetDialog(
    pet: Pet?,
    onDismiss: () -> Unit,
    onSave: (Pet) -> Unit
) {
    var petName by remember { mutableStateOf(pet?.name ?: "") }
    var petSpecies by remember { mutableStateOf(pet?.species ?: "") }
    var petBreed by remember { mutableStateOf(pet?.breed ?: "") }
    var selectedDate by remember { mutableStateOf(pet?.birthDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val today = System.currentTimeMillis()
    val isValid = petName.length >= 2 && petSpecies.isNotBlank() &&
            petBreed.isNotBlank() && selectedDate != null && selectedDate!! < today

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pet == null) "Dodaj zwierzaka" else "Edytuj zwierzaka") },
        text = {
            Column {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Imię") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && petName.length < 2,
                    supportingText = if (showError && petName.length < 2) {
                        { Text("Min. 2 znaki") }
                    } else null
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = petSpecies,
                    onValueChange = { petSpecies = it },
                    label = { Text("Gatunek") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && petSpecies.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = petBreed,
                    onValueChange = { petBreed = it },
                    label = { Text("Rasa") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && petBreed.isBlank()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectedDate?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = { },
                    label = { Text("Data urodzenia") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = showError && (selectedDate == null || (selectedDate ?: 0) >= today),
                    supportingText = if (showError) {
                        when {
                            selectedDate == null -> { { Text("Wybierz datę") } }
                            selectedDate!! >= today -> { { Text("Data w przyszłości") } }
                            else -> null
                        }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                                contentDescription = "Wybierz datę"
                            )
                        }
                    }
                )

                if (showError && !isValid) {
                    Text(
                        text = "Popraw błędy w formularzu",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isValid) {
                        showError = true
                    } else {
                        val newPet = Pet(
                            id = pet?.id ?: Pet.generateId(),
                            name = petName,
                            species = petSpecies,
                            breed = petBreed,
                            birthDate = selectedDate!!,
                            createdAt = pet?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(newPet)
                    }
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}