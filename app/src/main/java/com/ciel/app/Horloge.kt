package com.ciel.app

import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

/** Pilote l'appli Horloge du téléphone (minuteurs et alarmes), sans l'afficher. */
object Horloge {

    fun executer(context: Context, commande: Commande): String {
        val reglages = Reglages(context)
        if (reglages.respecterDnd && isDndActif(context) && (commande is Commande.Alarme || commande is Commande.Minuteur)) {
            return "Action annulée : mode « Ne pas déranger » actif."
        }

        return when (commande) {
            is Commande.Minuteur -> {
                lancer(context, Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, commande.secondes)
                    if (commande.message.isNotEmpty()) putExtra(AlarmClock.EXTRA_MESSAGE, commande.message)
                })
                "Minuteur de ${commande.secondes} s lancé"
            }
            is Commande.Alarme -> {
                lancer(context, Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, commande.heure)
                    putExtra(AlarmClock.EXTRA_MINUTES, commande.minute)
                    if (commande.message.isNotEmpty()) putExtra(AlarmClock.EXTRA_MESSAGE, commande.message)
                })
                "Alarme réglée à %02d:%02d".format(commande.heure, commande.minute)
            }
            is Commande.Meteo -> {
                MeteoClient.getMeteo("Toulon")
            }
            Commande.Statut -> "Ciel est en marche"
        }
    }

    private fun isDndActif(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.let { it.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL } ?: false
    }

    private fun lancer(context: Context, intent: Intent) {
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            throw IllegalStateException("Aucune appli Horloge ne gère cette action sur ce téléphone.")
        }
    }
}
