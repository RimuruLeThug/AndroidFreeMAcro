package com.ciel.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CielDashboard() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Ciel Android Agent", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Statut du serveur: En cours d'exécution")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* Action pour arrêter le service */ }) {
            Text("Arrêter le serveur")
        }
    }
}
