package com.example.tasknest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tasknest.settings.UserPreferences
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip

@Composable
fun SettingsScreen(prefs: UserPreferences) {
    var nameInput by remember { mutableStateOf(prefs.userName) }
    var savedMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it; savedMessage = "" },
            label = { Text("Your name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                prefs.updateUserName(nameInput)
                savedMessage = "Name saved!"
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }

        if (savedMessage.isNotEmpty()) {
            Text(savedMessage, color = MaterialTheme.colorScheme.primary)
        }
        Text("Default priority for new tasks", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Low", "Medium", "High").forEach { option ->
                FilterChip(
                    selected = prefs.defaultPriority == option,
                    onClick = { prefs.updateDefaultPriority(option) },
                    label = { Text(option) }
                )
            }
        }

        Text("Theme", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("System", "Light", "Dark").forEach { option ->
                FilterChip(
                    selected = prefs.themeMode == option,
                    onClick = { prefs.updateThemeMode(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}