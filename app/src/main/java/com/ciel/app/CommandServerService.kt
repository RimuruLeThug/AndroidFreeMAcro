package com.ciel.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Service au premier plan qui héberge le serveur HTTP local et exécute les commandes de Ciel. */
class CommandServerService : Service() {
    private var serveur: MiniServeurHttp? = null
    private val principal = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        val canal = NotificationChannel(CANAL, "Serveur Ciel", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reglages = Reglages(this)
        ServiceCompat.startForeground(
            this, 1, notification(reglages.port),
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        )
        if (serveur?.actif != true) {
            try {
                serveur = MiniServeurHttp(reglages.port) { requete -> repondre(requete, reglages) }.also { it.demarrer() }
                enMarche = true
                erreur = null
            } catch (e: Exception) {
                Log.e(TAG, "Démarrage du serveur impossible", e)
                erreur = "Port ${reglages.port} indisponible : ${e.message}"
                enMarche = false
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun repondre(requete: Requete, reglages: Reglages): Reponse {
        Log.i(TAG, "Requête reçue : ${requete.chemin} ${requete.params}")
        val auth = requete.entetes["authorization"]
        val resultat = CielCommandProcessor.traiter(requete.chemin, requete.params, auth, reglages.jeton)
        Log.d(TAG, "Résultat du traitement : $resultat")
        return when (val r = resultat) {
            is Resultat.Refuse -> {
                Log.w(TAG, "Requête refusée : ${requete.chemin}")
                Reponse(401, MiniServeurHttp.json(false, "Jeton absent ou invalide"))
            }
            is Resultat.Erreur -> {
                Log.e(TAG, "Erreur lors du traitement (${r.code}) : ${r.message}")
                Reponse(r.code, MiniServeurHttp.json(false, r.message))
            }
            is Resultat.Ok -> {
                Log.i(TAG, "Commande validée : ${r.commande}")
                // Les intents de l'Horloge partent du fil principal ; on attend le résultat pour le renvoyer à Ciel.
                var message = ""
                var echec: String? = null
                val fini = CountDownLatch(1)
                principal.post {
                    try {
                        message = Horloge.executer(this, r.commande)
                        Log.i(TAG, "Commande exécutée avec succès : $message")
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur lors de l'exécution de la commande", e)
                        echec = e.message ?: e.toString()
                    }
                    fini.countDown()
                }
                if (!fini.await(5, TimeUnit.SECONDS)) {
                    Log.e(TAG, "Délai dépassé pour l'exécution de ${r.commande}")
                    echec = "Délai dépassé"
                }
                val e = echec
                dernier = e ?: message
                if (e != null) Reponse(500, MiniServeurHttp.json(false, e))
                else Reponse(200, MiniServeurHttp.json(true, message))
            }
        }
    }

    private fun notification(port: Int): Notification {
        val ouvrir = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CANAL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Ciel écoute les commandes")
            .setContentText("Minuteurs et alarmes — port $port")
            .setContentIntent(ouvrir)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serveur?.arreter()
        serveur = null
        enMarche = false
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Ciel"
        private const val CANAL = "ciel_serveur"
        @Volatile var enMarche = false
        @Volatile var erreur: String? = null
        @Volatile var dernier: String? = null

        fun demarrer(context: Context) {
            Reglages(context).actif = true
            ContextCompat.startForegroundService(context, Intent(context, CommandServerService::class.java))
        }

        fun arreter(context: Context) {
            Reglages(context).actif = false
            context.stopService(Intent(context, CommandServerService::class.java))
        }
    }
}
