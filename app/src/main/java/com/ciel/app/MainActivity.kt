package com.ciel.app

import android.Manifest
import android.app.AlarmManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ciel.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.net.Inet4Address
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Reglages(this).actif && !CommandServerService.enMarche) CommandServerService.demarrer(this)
        setContent { MyApplicationTheme { Scaffold { p -> Ecran(Modifier.padding(p)) } } }
    }

    private fun adresseIp(): String {
        val cm = getSystemService(ConnectivityManager::class.java)
        val lien = cm.getLinkProperties(cm.activeNetwork) ?: return "pas de réseau"
        return lien.linkAddresses.map { it.address }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
            ?.hostAddress ?: "inconnue"
    }

    @Composable
    private fun Ecran(modifier: Modifier) {
        val reglages = remember { Reglages(this) }
        var jeton by remember { mutableStateOf(reglages.jeton) }
        var tic by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) { while (true) { delay(1000); tic++ } }

        val enMarche = tic.let { CommandServerService.enMarche }
        val superposition = tic.let { Settings.canDrawOverlays(this) }
        val batterie = tic.let {
            getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        }
        val ip = remember(tic / 5) { adresseIp() }
        val url = "http://$ip:${reglages.port}"
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = remember(tic / 5) { alarmManager.nextAlarmClock }

        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ciel", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            
            // Server Status Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).background(if (enMarche) Color.Green else Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (enMarche) "Serveur : EN MARCHE" else "Serveur : Arrêté", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(onClick = {
                        if (enMarche) CommandServerService.arreter(this@MainActivity)
                        else CommandServerService.demarrer(this@MainActivity)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (enMarche) "Arrêter le serveur" else "Démarrer le serveur")
                    }
                    
                    CommandServerService.erreur?.let { 
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) 
                    }
                }
            }

            // Visual Indicator for Active Alarms
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = if (nextAlarm != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Replaced Icon usage
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Prochaine alarme", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = if (nextAlarm != null) {
                                val date = Date(nextAlarm.triggerTime)
                                SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(date)
                            } else {
                                "Aucune alarme programmée"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Config Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Configuration", style = MaterialTheme.typography.titleSmall)
                    Text("Adresse : $url", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    Text("Jeton : $jeton", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Respecter le mode DND", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = reglages.respecterDnd,
                            onCheckedChange = { reglages.respecterDnd = it }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { copier("url=$url\njeton=$jeton") }, modifier = Modifier.weight(1f)) { Text("Copier") }
                        OutlinedButton(onClick = { jeton = reglages.nouveauJeton() }, modifier = Modifier.weight(1f)) { Text("Nouveau jeton") }
                    }
                }
            }

            // Permissions
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Permissions", style = MaterialTheme.typography.titleSmall)
                    PermissionRow("Superposition", superposition) {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    }
                    PermissionRow("Batterie (Sauf optim.)", batterie) {
                        startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
                    }
                }
            }

            // Last Activity
            CommandServerService.dernier?.let {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Dernière action", style = MaterialTheme.typography.titleSmall)
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    @Composable
    private fun PermissionRow(label: String, ok: Boolean, onClick: () -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (ok) {
                Text("OK", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            } else {
                TextButton(onClick = onClick) {
                    Text("ACCORDER", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    private fun copier(texte: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Ciel", texte))
    }
}
