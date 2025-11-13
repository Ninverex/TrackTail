package com.example.tracktail.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracktail.MainScreen
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

@Composable
fun PetDataScreen() {
    var petName by remember { mutableStateOf("") }
    var petSpecies by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petBirthday by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dane Twojego pupila",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = petName,
            onValueChange = { petName = it },
            label = { Text("Imię") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = petSpecies,
            onValueChange = { petSpecies = it },
            label = { Text("Gatunek") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = petBreed,
            onValueChange = { petBreed = it },
            label = { Text("Rasa") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = petBirthday,
            onValueChange = { petBirthday = it },
            label = { Text("Data urodzenia (dd/MM/yyyy)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // TODO: Zapis danych w JSON lub SharedPreferences
                println("Pet saved: $petName, $petSpecies, $petBreed, $petBirthday")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Zapisz", fontSize = 18.sp)
        }
    }
}