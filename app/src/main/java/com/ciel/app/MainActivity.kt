package com.ciel.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ciel.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.net.Inet4Address

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
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

        // Relu à chaque tic : état du service et autorisations
        val enMarche = tic.let { CommandServerService.enMarche }
        val superposition = tic.let { Settings.canDrawOverlays(this) }
        val batterie = tic.let {
            getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        }
        val ip = remember(tic / 5) { adresseIp() }
        val url = "http://$ip:${reglages.port}"

        Column(
            modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Ciel — minuteurs et alarmes", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (enMarche) "Serveur : EN MARCHE" else "Serveur : arrêté",
                style = MaterialTheme.typography.titleMedium,
                color = if (enMarche) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            CommandServerService.erreur?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                if (enMarche) CommandServerService.arreter(this@MainActivity)
                else CommandServerService.demarrer(this@MainActivity)
            }) { Text(if (enMarche) "Arrêter le serveur" else "Démarrer le serveur") }

            HorizontalDivider()
            Text("Adresse : $url", fontFamily = FontFamily.Monospace)
            Text("Jeton : $jeton", fontFamily = FontFamily.Monospace)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { copier("url=$url\njeton=$jeton") }) { Text("Copier adresse + jeton") }
                OutlinedButton(onClick = { jeton = reglages.nouveauJeton() }) { Text("Nouveau jeton") }
            }

            HorizontalDivider()
            Text("Autorisations nécessaires", style = MaterialTheme.typography.titleMedium)
            Text("Superposition (lancer l'Horloge en arrière-plan) : " + if (superposition) "OK" else "À ACCORDER")
            if (!superposition) Button(onClick = {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }) { Text("Autoriser la superposition") }
            Text("Optimisation batterie désactivée : " + if (batterie) "OK" else "À FAIRE")
            if (!batterie) Button(onClick = {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
                )
            }) { Text("Ne pas optimiser la batterie") }

            HorizontalDivider()
            OutlinedButton(onClick = {
                CommandServerService.dernier = try {
                    Horloge.executer(this@MainActivity, Commande.Minuteur(10, "Test Ciel"))
                } catch (e: Exception) {
                    e.message
                }
            }) { Text("Tester : minuteur 10 s") }
            CommandServerService.dernier?.let { Text("Dernière action : $it") }

            HorizontalDivider()
            Text("Exemples (depuis le PC, même wifi) :", style = MaterialTheme.typography.titleSmall)
            Text(
                "$url/minuteur?secondes=300&message=Pates&token=…\n$url/alarme?heure=7&minute=30&token=…\n$url/status",
                fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall
            )
        }
    }

    private fun copier(texte: String) {
        getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Ciel", texte))
    }
}
